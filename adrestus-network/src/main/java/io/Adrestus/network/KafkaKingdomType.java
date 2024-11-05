package io.Adrestus.network;

public enum KafkaKingdomType {
    ZOOKEEPER("ZOOKEEPER"),
    BROKER("BROKER"),
    PRODUCER("PRODUCER"),
    TOPIC_CREATOR("TOPIC_CREATOR"),
    CONSUMER_PRIVATE("CONSUMER_PRIVATE"),
    CONSUMER_SAME("CONSUMER_SAME");

    private final String title;

    KafkaKingdomType(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    public static KafkaKingdomType fromString(String text) {
        for (KafkaKingdomType b : KafkaKingdomType.values()) {
            if (b.title.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
