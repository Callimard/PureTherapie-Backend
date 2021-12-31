package puretherapie.crm.data.note;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNote", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "note", nullable = false)
    private String text;

    @Column(name = "level", nullable = false)
    private Integer level;
}