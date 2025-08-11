package pt.psoft.g1.psoftg1.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "book.recommendation.age.threshold")
@Getter
@Setter
public class AgeThresholdProperties {
    private int child;
    private int juvenile;
}
