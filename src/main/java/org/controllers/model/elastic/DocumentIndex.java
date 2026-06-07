package org.controllers.model.elastic;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "#{@elasticsearchProperties.index.name}")
@Setting(settingPath = "elasticsearch/document-settings.json")
@Mapping(mappingPath = "elasticsearch/document-mapping.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String filename;

    @Field(type = FieldType.Keyword)
    private String fileType;

    @Field(type = FieldType.Keyword)
    private String fileExtension;

    @Field(type = FieldType.Long)
    private Long fileSize;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime uploadedAt;

    @Field(type = FieldType.Long)
    private Long documentId;

    @Field(type = FieldType.Keyword, index = false)
    private String filePath;
}
