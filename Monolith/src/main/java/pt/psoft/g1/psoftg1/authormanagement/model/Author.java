package pt.psoft.g1.psoftg1.authormanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Name;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Entity
public class Author extends EntityWithPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUTHOR_NUMBER")
    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Embedded
    private Name name;

    @Embedded
    private Bio bio;

    @Column(unique = true, nullable = false, length = 24)
    private String hexId;

    @Column(unique = true, nullable = false, length = 20)
    private String businessId;

    @Column(unique = true, nullable = false)
    private Long customIncrementalId;

    public void setName(String name) {
        this.name = new Name(name);
    }

    public void setBio(String bio) {
        this.bio = new Bio(bio);
    }

    public Long getVersion() {
        return version;
    }

    public Long getId() {
        return authorNumber;
    }

    public Author(String name, String bio, String photoURI) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
        this.hexId = generateHexId();
        this.businessId = generateBusinessId(this.hexId);
        this.customIncrementalId = generateCustomIncrementalId();
    }
    private static final AtomicLong incrementalCounter = new AtomicLong(1); // Replace with DB or Redis in production

    public String generateHexId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24);
    }

    public String generateBusinessId(String input) {
        String hash = DigestUtils.sha256Hex(input);
        return hash.replaceAll("[^a-zA-Z0-9]", "").substring(0, 20);
    }

    public Long generateCustomIncrementalId() {
        return incrementalCounter.getAndIncrement();
    }

    protected Author() {
        this.hexId = generateHexId();
        this.businessId = generateBusinessId(this.hexId);
        this.customIncrementalId = generateCustomIncrementalId();
    }


    public void applyPatch(final long desiredVersion, final UpdateAuthorRequest request) {
        if (this.version != desiredVersion)
            throw new StaleObjectStateException("Object was already modified by another user", this.authorNumber);
        if (request.getName() != null)
            setName(request.getName());
        if (request.getBio() != null)
            setBio(request.getBio());
        if(request.getPhotoURI() != null)
            setPhotoInternal(request.getPhotoURI());
    }

    public void removePhoto(long desiredVersion) {
        if(desiredVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        setPhotoInternal(null);
    }
    public String getName() {
        return this.name.toString();
    }

    public String getBio() {
        return this.bio.toString();
    }

    public Long getAuthorNumber() {
        return authorNumber;
    }

    public void setAuthorNumber(Long authorNumber) {
        this.authorNumber = authorNumber;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public void setBio(Bio bio) {
        this.bio = bio;
    }

    public String getHexId() {
        return hexId;
    }

    public void setHexId(String hexId) {
        this.hexId = hexId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Long getCustomIncrementalId() {
        return customIncrementalId;
    }

    public void setCustomIncrementalId(Long customIncrementalId) {
        this.customIncrementalId = customIncrementalId;
    }
}

