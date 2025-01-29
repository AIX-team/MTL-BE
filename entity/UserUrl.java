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
@Table(name = "user_url")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUrl extends BaseTimeEntity {
    
    @EmbeddedId
    private UserUrlId id;
    
    @MapsId("email")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    @MapsId("urlId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url;
    
    private boolean isUse;
    
    @Builder
    public UserUrl(User user, Url url) {
        this.id = new UserUrlId(user.getEmail(), url.getId());
        this.user = user;
        this.url = url;
        this.isUse = true;
    }
} 