package nl.hu.s3.project.chips;

public interface ChipsRepository {
    ChipsAccount findByUsername(String username);

    ChipsAccount findOrCreate(String username);
}
