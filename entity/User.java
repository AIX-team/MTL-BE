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
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    
    @Id
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    private String profileImg;
    
    private boolean isDelete;
    
    @OneToMany(mappedBy = "user")
    private List<UserSearchTerm> searchTerms = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<TravelTaste> travelTastes = new ArrayList<>();
    
    @Builder
    public User(String email, String name, String profileImg) {
        this.email = email;
        this.name = name;
        this.profileImg = profileImg;
        this.isDelete = false;
    }
} 