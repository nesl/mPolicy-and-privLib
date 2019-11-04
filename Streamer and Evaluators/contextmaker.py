
import datetime
import numpy
# Create a context stream to attach
#  Assumes a Markov chain model for each different type of context



class contextStream:

    # Convert millisecond timestamp into an hour
    def convert_to_hour(self, timestamp_ms):
        date = datetime.datetime.fromtimestamp(timestamp_ms/1000)
        return date.hour

    # Convert ms timestamp to MM/DD/YYYY
    def convert_to_date(self, timestamp_ms):
        date = datetime.datetime.fromtimestamp(timestamp_ms/1000)
        return str(date.month) + "/" + str(date.day) + "/" + str(date.year)

    #  For now, just assume the following context streams:
    #  Time (both timestamp and larger granularity)
    #  GPS  (longitude/latitude) Truncating reduces accuracy: https://gis.stackexchange.com/questions/8650/measuring-accuracy-of-latitude-and-longitude
    #  Button Presses (0, or 1 for active)
    def __init__(self):
        self.possible_gps_locations = [[34.070143, -118.444720], [34.062497, -118.447253],
        [34.058019, -118.446220]]
        self.time_ms = 0
        self.time_hour = 0
        self.time_date = ""
        self.gps = self.possible_gps_locations[0]
        self.gps_index = 0  # This is the index of the possible gps locations
        self.button_press = 0

        # These are the transition probabilities to be used for the markov chain
        self.transition_probabilities_gps = {
            0: {0: 0.85, 1: 0.05, 2: 0.1}, 1: {0: 0.01, 1: 0.98, 2: 0.01}, 2: {0: 0.02, 1: 0.01, 2: 0.97}
        }
        self.transition_probabilities_button = {
            0: {0: 0.98, 1: 0.02}, 1: {0: 0.05,1: 0.95}
        }

        self.headers = ["time_ms", "time_hour", "time_date", "gps_latitude", "gps_longitude", "button_press"]

    # Choose a random state given a transition probability and current state
    def choose_random_state(self, transition_probability, current_state):
        potential_states = list(transition_probability[current_state].keys())
        state_probabilities = list(transition_probability[current_state].values())
        newstate = numpy.random.choice(potential_states, 1, p=state_probabilities)
        return newstate[0]

    # Choose new states for all contexts
    def change_new_states(self, start_timestamp):
        # Get new times
        self.time_ms = start_timestamp
        self.time_hour = self.convert_to_hour(start_timestamp)
        self.time_date = self.convert_to_date(start_timestamp)
        # Get new GPS location
        self.gps_index = self.choose_random_state(self.transition_probabilities_gps, self.gps_index)
        self.gps = self.possible_gps_locations[self.gps_index]
        self.button_press = self.choose_random_state(self.transition_probabilities_button, self.button_press)

    # Get the context values at the current point in time.
    def get_context_list(self):
        datarow = [self.time_ms, self.time_hour, self.time_date, self.gps[0], self.gps[1], self.button_press]
        return datarow

    # def write_context_file(self, file, start_timestamp):
    #     self.get_new_states(start_timestamp)
    #     file.write(str(get_context_list)[1:-1] + "\n")
