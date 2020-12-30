from faker import Faker
from time import sleep


if __name__ == '__main__':
    faker = Faker() 
    fields = ['job']
    # fields = ['job','company','residence','username','name','sex','address','mail','birthdate','ssn']

    while True: 
        data = faker.profile(fields)
        print(f"Inserting data {data}")
        sleep(1)