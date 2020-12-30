# pip install confluent-kafka
from confluent_kafka import Producer
from faker import Faker
from time import sleep
# from confluent_kafka.avro import AvroProducer


# in.flight.requests.per.session=1 :
# guaranteeing order. This will severely limit the throughput of the producer.
# Asynchronous writes
def acked(err, msg):
    if err is not None:
        print("Failed to deliver message: %s: %s" % (str(msg), str(err)))
    else
        print("Message send successfully: %s" % (str(msg)))
conf = {'bootstrap.servers': "localhost:9092",
        'client.id': 'tiw_producer'}
# "schema.registry.url": schemaUrl

# schema = { "namespace": "customerManagement.avro",
#     "type": "record",
#     "name": "Customer",
#     "fields": [
#         {"name": "id", "type": "int"},
#         {"name": "name", "type": "string"},
#         {"name": "faxNumber", "type": ["null", "string"], "default": "null"}
#     ]
# }

producer = Producer(conf)
topic = "job-topic"
faker = Faker() 
fields = ['job']
order = 1
try: 
    while True: 
        data = faker.profile(fields)
        key = "job{}".format(order)
        print(" data {'{0}': '{1}'}".format(key, data['job']))
        producer.produce(topic, key=key, value=data['job'], callback=acked)
        order += 1
        sleep(1)
except KeyboardInterrupt:
    break

producer.poll(0.5)
