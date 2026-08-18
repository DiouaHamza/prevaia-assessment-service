package msa.emaia.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FileStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String filename;
    private String mime_type;
    @Column(name = "assessment_id")
    private String assessment_id;

    @Column(name = "content", columnDefinition="bytea")
    private byte[] content;
}
