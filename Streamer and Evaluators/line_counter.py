# Also plot the results
import matplotlib.pyplot as plt

def avg(some_list):
    return int(sum(some_list) / len(some_list))

# Reads the raw file from Android and counts how many lines of data are in between timestamps

num_policy_list = []
intervals_list = []
start_times_list = []
end_times_list = []

# Dictionary of num_policies,intervals as keys and the number of data samples as the value
result_counts = {}

result_files = ["RESULTS_" + str(x) + ".txt" for x in range(5)]  # ONLY GET 5 FILES

for file_name in result_files:
    read_results_file = open(file_name, "r")
    # Read each line, and store each result into its corresponding list
    line = read_results_file.readline()
    while line:
       items = line.strip().split(" ")
       num_policy_list.append(int(items[0]))
       intervals_list.append(int(items[1]))
       start_times_list.append(int(items[2]))
       end_times_list.append(int(items[3]))

       result_counts[items[0] + "_" + items[1]] = [0 for x in result_files] # Make a zero for each file

       line = read_results_file.readline()

    read_results_file.close()

read_data_file = open("RAW.txt", "r")
line = read_data_file.readline()

min_timestamp = -1;
max_timestamp = 0;
total_lines = 0
previous_timestamp = 0
num_adds = 0

while line:
    # Convert the line to a timestamp
    timestamp = int(line.strip())
    if min_timestamp < 0:
        min_timestamp = timestamp
    if timestamp > max_timestamp:
        max_timestamp = timestamp
    #Iterate through each start and end time.
    for i in range(0, len(start_times_list)):

        # Add to the count if timestamp is in this range
        if start_times_list[i] <= timestamp and timestamp <= end_times_list[i]:
            # Basically add 1 to each policy/frequency id corresponding to the correct file number
            result_counts[str(num_policy_list[i]) + "_" + str(intervals_list[i])][i//len(list(result_counts.keys()))] += 1
            num_adds += 1
            # print(i)
            # print(str(num_policy_list[i]) + "_" + str(intervals_list[i]))
    total_lines += 1
    line = read_data_file.readline()

read_data_file.close()

print(min_timestamp)
print(max_timestamp)
print(result_counts)
print("total lines: " + str(total_lines))

# Plot the results

# result_counts is a dictionary of num_policies,intervals as keys and the number of data samples as the value

# Plot two things:
# 1 - change in number of samples over change in number of policies
# 2 - change in number of samples over change in frequency of evaluations
new_result_counts = {}
for key in result_counts:
    new_result_counts[key] = avg(result_counts[key])
print("NEW: " + str(new_result_counts))
result_keys = new_result_counts.keys()
# Get all the keys of the same policy
result_num_policies = []
result_frequencies = []

for r_key in result_keys:
     current_num_policy = int(r_key.split("_")[0])
     current_frequency = int(r_key.split("_")[1])

     # Only care about the ones divisible by 10, and also remove 10 because it seems off
     if current_num_policy not in result_num_policies and not current_num_policy % 10 > 0 and not current_num_policy==10:
         result_num_policies.append(current_num_policy)
     if current_frequency not in result_frequencies:
         result_frequencies.append(current_frequency)

# be sure to sort each of these
result_num_policies.sort()
result_frequencies.sort(reverse=True)  # So we have decreasing samples

# If the frequency value is -1, it means we never evaluate any policies, so it should be
#  greater than all the other frequencies
num_seconds = 60
data_samples_per_policy = [ new_result_counts[str(x)+"_10"]/num_seconds for x in result_num_policies]
data_samples_per_frequency = [ new_result_counts["15_"+str(x)]/num_seconds for x in result_frequencies]

# We also want to add 0 policies.  Any odd number will suffice
chosen_index = 25
result_num_policies.insert(0,0)
data_samples_per_policy.insert(0,new_result_counts[str(chosen_index)+"_100"]/num_seconds)

# We also want to add no sampling.
chosen_frequency = -1
# First remove the already existing -1 sample
result_frequencies = result_frequencies[:-1]
data_samples_per_frequency = data_samples_per_frequency[:-1]
result_frequencies.append(chosen_frequency)
data_samples_per_frequency.append(new_result_counts["25_"+str(chosen_frequency)]/num_seconds)

print(result_num_policies)
print(data_samples_per_policy)
print(result_frequencies)
print(data_samples_per_frequency)

# # Number of data samples for each number of policies at every 100ms
#fig, ax = plt.subplots(result_num_policies,  data_samples_per_policy)
# plt.plot(result_num_policies,  data_samples_per_policy)
# locs, labels = plt.xticks()            # Get locations and labels
# # # New labels
# # labels = [str(x) for x in labels]
# # labels[0] = "None"
# # plt.xticks(locs, labels)
# labels = [str(x) for x in result_num_policies]
# labels.insert(1, "None")
# plt.xticks(locs, labels)
# # ax.set_xticklabels(labels)
# plt.xlabel('Number of Policies Evaluated')
# plt.ylabel('Number of sensor samples (Hz)')
# #plt.title('Impact of Evaluating Policies during Sensor Streaming')
# plt.show()
#
# # Number of data samples for each frequency for 10 policies
# #fig, ax = plt.subplots(result_frequencies, data_samples_per_frequency )
# plt.plot(result_frequencies, data_samples_per_frequency )
# locs, labels = plt.xticks()            # Get locations and labels
# # # New labels
# # labels = [str(x) for x in labels]
# # labels[-1] = "Never"
# # plt.xticks(locs, labels)
# labels = [str(x) for x in result_frequencies]
# plt.xscale('log',basex=10)
# labels[-1] = "Never"
# plt.xticks(locs, labels)
# # ax.set_xticklabels(labels)
#
# plt.xlabel('Frequency of Evaluation (ms)')
# plt.ylabel('Number of sensor samples (Hz)')
# #plt.title('Impact of Evaluating Policies during Sensor Streaming')
# plt.show()
