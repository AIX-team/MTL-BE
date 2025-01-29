import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "url")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Url extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    private String extUrlId;
    private String urlTitle;
    private String urlAuthor;
    
    @Column(nullable = false)
    private String url;
    
    @OneToMany(mappedBy = "url")
    private List<UserUrl> userUrls = new ArrayList<>();
    
    @Builder
    public Url(String extUrlId, String urlTitle, String urlAuthor, String url) {
        this.extUrlId = extUrlId;
        this.urlTitle = urlTitle;
        this.urlAuthor = urlAuthor;
        this.url = url;
    }
} 