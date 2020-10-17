package dev.vuongdang.springdataelasticsearchexample.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@Document(indexName="books")
public class Book {
    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, fielddata = true),
        otherFields = {
                @InnerField(suffix = "raw", type = FieldType.Keyword)
        }
    )
    private String name;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Object)
    private List<Author> authors;
}
