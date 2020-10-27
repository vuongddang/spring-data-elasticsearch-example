package dev.vuongdang.springdataelasticsearchexample.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
public class Author {
    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, fielddata = true),
        otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
        }
    )
    private String name;
}
