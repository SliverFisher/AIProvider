package com.aiprovider.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class MaidAvatarService {

    private final Path avatarDirectory;

    public MaidAvatarService(@Value("${maid.avatar-directory:/opt/aiprovider/characters}") String avatarDirectory) {
        this.avatarDirectory = Paths.get(avatarDirectory).toAbsolutePath().normalize();
    }

    public Resource find(String roleId) throws IOException {
        if (roleId == null || !roleId.matches("[A-Za-z0-9_-]{1,96}") || !Files.isDirectory(avatarDirectory)) {
            return null;
        }
        String expectedStem = roleId.toLowerCase(Locale.ROOT);
        try (Stream<Path> files = Files.walk(avatarDirectory, 3)) {
            Optional<Path> match = files
                    .filter(Files::isRegularFile)
                    .filter(path -> isImage(path) && stem(path).equalsIgnoreCase(expectedStem))
                    .findFirst();
            return match.isPresent() ? new FileSystemResource(match.get().toFile()) : null;
        }
    }

    private static boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp");
    }

    private static String stem(Path path) {
        String name = path.getFileName().toString();
        int extension = name.lastIndexOf('.');
        return extension > 0 ? name.substring(0, extension) : name;
    }
}
