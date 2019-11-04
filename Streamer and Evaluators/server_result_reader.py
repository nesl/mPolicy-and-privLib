
import matplotlib.pyplot as plt
import numpy as np


policy_dict = {}

read_results_file = open("server_results.txt", "r")
# Read each line, and store each result into its corresponding list

contents = read_results_file.read()

policy_results = contents.split("-")
for policy_result in policy_results:

    #print(policy_result)
    policy_lines = policy_result.strip().split("\n")
    print(policy_lines)
    current_num_policies = int(policy_lines[0].split(":")[1])
    parse_time = int(policy_lines[1].split(":")[1]) / 1000000
    p_stream_creation_time = int(policy_lines[2].split(":")[1]) / 1000000
    query_time = int(policy_lines[3].split(":")[1]) / 1000000
    policy_combine_time = int(policy_lines[4].split(":")[1]) / 1000000

    #  0 is parse time, 1 is stream creation time, 2 is query time, 3 is policy combine time
    policy_dict[current_num_policies] = [parse_time, p_stream_creation_time, query_time, policy_combine_time]

read_results_file.close()

policy_keys = list(policy_dict.keys())
# Remember to sort the keys
policy_keys.sort()

print(policy_keys)
print([ policy_dict[x][0]  for x in policy_keys])
print([ policy_dict[x][1]  for x in policy_keys])
print([ policy_dict[x][2]  for x in policy_keys])
print([ policy_dict[x][3]  for x in policy_keys])

y_pos = np.arange(len(policy_keys))

# Plot the parse time
plt.bar(policy_keys, [ policy_dict[x][0]  for x in policy_keys], align='center', alpha=0.5)
plt.xticks(policy_keys)
plt.xlabel('Number of Policies Evaluated')
plt.ylabel('Operation time (ms)')
plt.title('Parsing Time for number of policies')
plt.show()

# Plot the stream create time
plt.bar(policy_keys, [ policy_dict[x][1]  for x in policy_keys], align='center', alpha=0.5)
plt.xticks(policy_keys)
plt.xlabel('Number of Policies Evaluated')
plt.ylabel('Operation time (ms)')
#plt.title('Policy Creation Time for number of policies')
plt.show()

# Plot the query time
plt.bar(policy_keys,  [ policy_dict[x][2]  for x in policy_keys], align='center', alpha=0.5)
plt.xticks(policy_keys)
plt.xlabel('Number of Policies Evaluated')
plt.ylabel('Operation time (ms)')
#plt.title('Query Time for number of policies')
plt.show()

# Plot the policy combine time
plt.bar(policy_keys, [ policy_dict[x][3]  for x in policy_keys], align='center', alpha=0.5)
plt.xticks(policy_keys)
plt.xlabel('Number of Policies Evaluated')
plt.ylabel('Operation time (ms)')
#plt.title('Policy Combining Time for number of policies')
plt.show()
