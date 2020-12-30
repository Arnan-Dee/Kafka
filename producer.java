Properties config = new Properties();
config.put("client.id", 'tiw_producer');
config.put("bootstrap.servers", "localhost:9092");
config.put("acks", "all");

producer = new KafkaProducer<String, String>(config);