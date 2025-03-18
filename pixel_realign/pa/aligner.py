#!/usr/bin/env python3

import pandas as pd
import numpy as np

import datetime as dt
import matplotlib.pyplot as plt

import pytz


TIME_FORMAT_STR = '%Y-%m-%d %H:%M:%S'

# Define custom aggregation functions
def get_min_time(x):
    return x.idxmin()

def get_max_time(x):
    return x.idxmax()


def get_time_with_time_zone(t, time_zone):
    tz = pytz.timezone(time_zone)
    return tz.localize(t)
    

class DataSeries:
    def __init__(self, params, line_color='black', label_color='red'):
        self.tag = params["tag"]
        self.filename = params["filename"]
        self.time_format = params["time_format"]
        self.time_zone = params.get("time_zone") or None

        if self.time_format.lower() == 'ms':
            t = dt.datetime.fromtimestamp(int(params["start_time"])/1000)
            self.start_time = get_time_with_time_zone(t, self.time_zone)

            t = dt.datetime.fromtimestamp(int(params["end_time"])/1000)
            self.end_time = get_time_with_time_zone(t, self.time_zone)

        else:
            t = dt.datetime.strptime(params["start_time"], self.time_format)
            self.start_time = get_time_with_time_zone(t, self.time_zone)
            
            t = dt.datetime.strptime(params["end_time"], self.time_format)
            self.end_time = get_time_with_time_zone(t, self.time_zone)
            
        self.window = params["window"]
        self.line_color = line_color
        self.label_color = label_color
        self.align_point = None
    
    def dump_params(self):
        print("filename:", self.filename)
        print("start_time:", self.start_time)
        print("end_time:", self.end_time)
        print("window:", self.window)


    def load_apple_hr(self):
        self.df = pd.read_csv(self.filename)
        self.df['time'] = pd.to_datetime(self.df['Date/Time'], format=self.time_format)
        self.df['time'] = self.df['time'].dt.tz_localize(self.time_zone)


        # rename the "Avg (count/min)" column to "Value"
        self.df = self.df.rename(columns={'Avg (count/min)': 'Value'})
        
        self.df = self.df[(self.df['time'] >= self.start_time) & (self.df['time'] <=self.end_time)]

    def load_pixel_hr(self):
        self.df = pd.read_csv(self.filename, names=['Local', 'Time', 'Value'])

        self.df['time'] = pd.to_datetime(self.df['Time'], utc=True, unit=self.time_format)
        self.df['time'] = self.df['time'].dt.tz_convert(self.time_zone)

        print(self.start_time, self.end_time, self.df['time'].iloc[0])
        self.df = self.df[(self.df['time'] >= self.start_time) & (self.df['time'] <=self.end_time)]

    def load_actigraph(self):
        self.df = pd.read_csv(self.filename, names=['timestamp', 'x', 'y', 'z'], skiprows=11)
        self.df['time'] = pd.to_datetime(self.df['timestamp'], format=self.time_format)
        self.df['time'] = self.df['time'].dt.tz_localize(self.time_zone)
        
        self.df = self.df[(self.df['time'] >= self.start_time) & (self.df['time'] <=self.end_time)]
        self.df['Value'] = np.sqrt(self.df["x"]**2 + self.df["y"]**2 + self.df["z"]**2)

    def load_pixel_accel(self):
        self.df = pd.read_csv(self.filename, names=['local', 'Time', 'x', 'y', 'z'])
        self.df['time'] = pd.to_datetime(self.df['Time'], utc=True, unit=self.time_format)

        self.df = self.df[(self.df['time'] >= self.start_time) & (self.df['time'] <=self.end_time)]
        self.df['Value'] = np.sqrt(self.df["x"]**2 + self.df["y"]**2 + self.df["z"]**2)/9.8
        
    def load_data(self):
        self.df = pd.read_csv(self.filename)
        self.df['time'] = pd.to_datetime(self.df['Time'], format='%m/%d/%Y %I:%M:%S %p')
        
        #df["T"] = df["Time"].apply(lambda x: dt.datetime.strptime(x, '%m/%d/%Y %I:%M:%S %p'))
        #print(df)


        self.df = self.df[(self.df['time'] >= self.start_time) & (self.df['time'] <=self.end_time)]


    def plot_relative(self, ax):
        t0 = self.df["time"].iloc[0]
        self.df['relative_time'] = (self.df['time'] - t0).dt.total_seconds()
        
        self.df.set_index('time', inplace=True)

        # Resample and apply custom aggregations
        self.df2 = self.df['Value'].resample(self.window).agg(
            min_value=('min'),
            max_value=('max'),
            time_of_min_value=(get_min_time),
            time_of_max_value=(get_max_time)
        )

        ax.plot(self.df["relative_time"], self.df["Value"], color=self.line_color, label=self.tag)

        # plot min values
        self.df2["relative_time_of_min_value"] = (self.df2["time_of_min_value"] - t0).dt.total_seconds()
        ax.scatter(self.df2["relative_time_of_min_value"], self.df2["min_value"], color=self.label_color)

        odd_labels = np.arange(1, 2*len(self.df2)+1, 2)
    
        # Adding text labels
        for label, (index, row) in zip(odd_labels, self.df2.iterrows()):
            ax.text(row['relative_time_of_min_value'], row['min_value'], str(label), color=self.label_color, fontsize=12)

        # plot max values
        self.df2["relative_time_of_max_value"] = (self.df2["time_of_max_value"] - t0).dt.total_seconds()
        ax.scatter(self.df2["relative_time_of_max_value"], self.df2["max_value"], color=self.label_color)
    
        even_labels = np.arange(0, 2*len(self.df2)+1, 2)
    
        # Adding text labels
        for label, (index, row) in zip(even_labels, self.df2.iterrows()):
            ax.text(row['relative_time_of_max_value'], row['max_value'], str(label), color=self.label_color, fontsize=12)
    
        ax.set_ylabel("Beats")
        ax.set_xlabel("Time")
        
    def get_ref_time(self):
        idx = self.align_point // 2

        if self.align_point % 2 == 0:
            ref_time = self.df2["time_of_max_value"].iloc[idx]
        else:
            ref_time = self.df2["time_of_min_value"].iloc[idx]

        return ref_time


    def re_align(self, timestamp):
        """
        Calculate time shift of the align_point based on the timestamp provided and re-align the data series.
        """
        t = self.get_ref_time()
        delta = timestamp - t

        self.df.index = self.df.index + delta

    def plot_absolute(self, ax):
        ax.plot(self.df.index, self.df["Value"], color=self.line_color, label=self.tag)
        ax.set_ylabel("Beats")
        ax.set_xlabel("Time")

    def hr2csv(self, filename):
        # convert datetime to timestamp in ms
        df = self.df[['Local', 'Value']].copy()  # Keep only 'Local' and 'Value'
        df.index = df.index.astype('int64') // 10**6
        df = df.reset_index()
        df = df[['Local', 'time', 'Value']]
        df.to_csv(filename, index=False, header=False)
        
    def accel2csv(self, filename):
        # convert datetime to timestamp in ms
        df = self.df.copy()  # Keep only 'Local' and 'Value'
        df.index = df.index.astype('int64') // 10**6
        df = df.reset_index()
        df = df[['local', 'time', 'x', 'y', 'z']]
        df.to_csv(filename, index=False, header=False)
