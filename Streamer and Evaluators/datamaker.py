
from streams import GPSStream, mainStream, IMUStream, HRVStream
from contextmaker import contextStream
# Initialize data and context streams.
gps_stream = GPSStream(1)
imu_stream = IMUStream(200)
hrv_stream = HRVStream(250)
context_stream = contextStream()

start_timestamp = 1572304074000  # 4th to last index is seconds
data_length = 10   #Generate X seconds of data

# Open files
gps_file = open("data/" + gps_stream.name + ".txt", "w")
imu_file = open("data/" + imu_stream.name + ".txt", "w")
hrv_file = open("data/" + hrv_stream.name + ".txt", "w")
context_file = open("data/context.txt", "w")

# Add stuff to the main streaming service
main_stream = mainStream(context_stream, context_file)
main_stream.add_stream(gps_stream, gps_file)
main_stream.add_stream(imu_stream, imu_file)
main_stream.add_stream(hrv_stream, hrv_file)

# Write the header to each streams
context_file.write(str(context_stream.headers)[1:-1] + "\n")
gps_file.write(str(gps_stream.headers)[1:-1] + "\n")
imu_file.write(str(imu_stream.headers)[1:-1] + "\n")
hrv_file.write(str(hrv_stream.headers)[1:-1] + "\n")

# Produce all sensor data
main_stream.produce_data_streams(start_timestamp, data_length)


gps_file.close()
imu_file.close()
hrv_file.close()
context_file.close()


#  Settings for each sensor stream
