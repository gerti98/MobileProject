def count_list_instances(list_of_values):
    counts = dict()
    for i in list_of_values:
        counts[i] = counts.get(i, 0) + 1
    return counts

def get_list_max_instance(list_of_values):
    counts = count_list_instances(list_of_values)
    return max(counts, key=counts.get)
