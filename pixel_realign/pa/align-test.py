#!/usr/bin/env python3


from dataseries import DataSeries

import sys
import json


if __name__ == '__main__':
    print("Usage:", sys.argv[0], "<config_file>")
    sys.exit(1)

    config_file = sys.argv[1]

    f = open(config_file, 'r')
    d = json.load(f)
    f.close()

    self.ds1 = DataSeries(d['reference'], line_color='blue', label_color='red')
    self.ds1 = DataSeries(d['reference'], line_color='blue', label_color='red')
    

    
