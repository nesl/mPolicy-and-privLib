

# reads in a policy as a file, and copies it over and over depending on the params
#  puts a new policy file into the same directory
def generateCopyPolicies(example_policy_filedir, example_policy_filename,
new_policy_filename, new_policy_id, num_copies=5):

    # Read file contents
    pol_file = open(example_policy_filedir + example_policy_filename, "r")
    pol_contents = pol_file.read()
    pol_file.close()

    #Write file
    new_pol_file = open(example_policy_filedir + new_policy_filename, "w+")
    current_policy_id = new_policy_id

    # Copy this policy however many times
    for i in range(num_copies):
         # Write contents from example file
        new_pol_file.write("\nPOLICY: " + str(current_policy_id) + "\n" + pol_contents)
        current_policy_id += 1

    new_pol_file.close()


# Generate policies of chosen size
num_policies = [100000]
filedir = "policies/"
policy_example_filename = "policy_example.txt"
policy_id = 566

for num_copies in num_policies:

    new_policy_filename = "policies_" + str(num_copies) + ".txt"
    # Generate copies
    generateCopyPolicies(filedir, policy_example_filename,
    new_policy_filename, policy_id, num_copies=num_copies)
