import random
from contextmaker import contextStream

from random import seed
from random import gauss
# seed random number generator
seed(1)

# Class for Sensor Streams
#  Should include a sampling rate
# class SensorStream:
#
#     def __init__(self, samping_rate):
#
#         self.sampling_rate = sampling_rate
#
#     # Generate one row of data
#     def generate_data_row():
#

# Class that determines, given a set of streams and sampling rates,
#  which ones are outputting data to the stream
class mainStream:

    def __init__(self, context_stream, context_file):
        self.context_stream = context_stream
        self.context_file = context_file
        self.data_streams = []
        self.data_stream_names = []
        self.data_stream_files = []

    # Add a stream to the list of streams for this main stream
    def add_stream(self, stream_to_add, stream_file):
        self.data_streams.append(stream_to_add)
        self.data_stream_names.append(stream_to_add.name)
        self.data_stream_files.append(stream_file)

    # Removes "[]" from data lists and converts them to strings
    def convert_data_to_str(self, data_list):
        return str(data_list)[1:-1] + "\n"

    # Generate a piece of data for a particular timestamp
    def write_data_for_timestamp(self, timestamp):

        # Get context information
        self.context_stream.change_new_states(timestamp)
        context_list = self.context_stream.get_context_list()

        # Check if any sensors were sampled. If not, we do not write to any files
        written = False

        # Check which data streams are emitting at this point in time
        for i, data_stream in enumerate(self.data_streams):
            stream_name = self.data_stream_names[i]
            stream_file = self.data_stream_files[i]

            # If this data stream is meant to sample, then sample
            if timestamp % data_stream.timestamp_difference == 0:

                if stream_name == "GPS":
                    data_values=data_stream.generate_data_row(timestamp, self.context_stream.gps[0],self.context_stream.gps[1])
                    stream_file.write(self.convert_data_to_str(data_values))
                    written = True
                elif stream_name == "IMU":
                    data_values=data_stream.generate_data_row(timestamp)
                    stream_file.write(self.convert_data_to_str(data_values))
                    written = True
                elif stream_name == "HRV":
                    data_values=data_stream.generate_data_row(timestamp)
                    stream_file.write(self.convert_data_to_str(data_values))
                    written = True

        # If sensors were sampled, we add to the context stream
        if written:
            self.context_file.write(self.convert_data_to_str(context_list))
            return True
        else:
            return False

    # Iterate and produce data for each stream (context stream included)
    def produce_data_streams(self, start_timestamp, seconds_to_generate, context_write_limit = 1000):

        ms_to_generate = seconds_to_generate*1000
        current_timestamp = start_timestamp
        num_context_writes = 0

        for i_ms in range(ms_to_generate):

            if(self.write_data_for_timestamp(current_timestamp)):
                num_context_writes += 1

            # If we write this many lines to the context stream, we stop.
            if(num_context_writes >= context_write_limit):
                break
            current_timestamp += 1

# Class for the GPS stream
class GPSStream:

    # Sampling rate in Hz
    def __init__(self, sampling_rate):
        self.sampling_rate = sampling_rate
        self.name = "GPS"
        self.headers = ["timestamp", "latitude", "longitude"]
        # Get the difference in time (milliseconds) for each sample
        #  Basically tells us how often this datastream should sample
        self.timestamp_difference = 1000 // self.sampling_rate  # round to nearest integer

    # Generate one row of data, comprised of timestamp, longitude and latitude
    def generate_data_row(self, timestamp, latitude, longitude):
        datarow = [timestamp, latitude, longitude]
        return datarow

class IMUStream:

    # Sampling rate in Hz
    def __init__(self, sampling_rate):
        self.sampling_rate = sampling_rate
        self.name = "IMU"
        self.headers = ["timestamp", "x", "y", "z"]
        # Get the difference in time (milliseconds) for each sample
        #  Basically tells us how often this datastream should sample
        self.timestamp_difference = 1000 // self.sampling_rate  # round to nearest integer

    #  Get a random accerlation value
    def get_random_value(self):
        return gauss(0, 1)

    # Generate one row of data, comprised of timestamp, longitude and latitude
    def generate_data_row(self, timestamp):
        datarow = [timestamp, self.get_random_value(), self.get_random_value(), self.get_random_value()]
        return datarow

class HRVStream:

    # Sampling rate in Hz
    def __init__(self, sampling_rate):
        self.sampling_rate = sampling_rate
        self.name = "HRV"
        self.headers = ["timestamp", "val"]
        # Get the difference in time (milliseconds) for each sample
        #  Basically tells us how often this datastream should sample
        self.timestamp_difference = 1000 // self.sampling_rate  # round to nearest integer

    #  Get a random accerlation value
    def get_random_value(self):
        return gauss(0, 1)

    # Generate one row of data, comprised of timestamp, longitude and latitude
    def generate_data_row(self, timestamp):
        datarow = [timestamp, self.get_random_value()]
        return datarow
