package com.aiprovider.service;

import com.aiprovider.model.vo.FileTransferDownload;
import com.aiprovider.model.vo.FileTransferFileVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileTransferService {
    private static final String TEMP_PREFIX = ".file-transfer-upload-";
    private final Path storageDirectory;

    public FileTransferService(@Value("${file-transfer.storage-directory}") String storageDirectory) {
        if (storageDirectory == null || storageDirectory.trim().isEmpty())
            throw new IllegalArgumentException("文件中转存储目录未配置");
        this.storageDirectory = Paths.get(storageDirectory).toAbsolutePath().normalize();
    }

    public FileTransferFileVO upload(MultipartFile file) throws IOException {
        if (file == null) throw new IllegalArgumentException("请选择要上传的文件");
        String fileName = validateFileName(file.getOriginalFilename());
        Files.createDirectories(storageDirectory);
        Path target = resolve(fileName);
        Path temporary = storageDirectory.resolve(TEMP_PREFIX + UUID.randomUUID() + ".tmp");
        try {
            file.transferTo(temporary);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return describe(target);
    }

    public List<FileTransferFileVO> list() throws IOException {
        Files.createDirectories(storageDirectory);
        try (Stream<Path> files = Files.list(storageDirectory)) {
            return files.filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString().startsWith(TEMP_PREFIX))
                .map(this::describeUnchecked)
                .sorted(Comparator.comparing(FileTransferFileVO::getUploadedAt).reversed()
                    .thenComparing(FileTransferFileVO::getFileName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        }
    }

    public FileTransferDownload download(String fileName) throws IOException {
        Path path = requireExisting(fileName);
        return new FileTransferDownload(path.getFileName().toString(), Files.size(path), new FileSystemResource(path));
    }

    public void delete(String fileName) throws IOException {
        Files.delete(requireExisting(fileName));
    }

    private Path requireExisting(String fileName) {
        Path path = resolve(validateFileName(fileName));
        if (!Files.isRegularFile(path)) throw new IllegalArgumentException("文件不存在：" + fileName);
        return path;
    }

    private Path resolve(String fileName) {
        Path resolved = storageDirectory.resolve(fileName).normalize();
        if (!resolved.getParent().equals(storageDirectory)) throw new IllegalArgumentException("文件名不合法");
        return resolved;
    }

    private String validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) throw new IllegalArgumentException("文件名不能为空");
        String value = fileName;
        if (value.equals(".") || value.equals("..") || value.indexOf('/') >= 0 || value.indexOf('\\') >= 0 || value.indexOf('\0') >= 0)
            throw new IllegalArgumentException("文件名不合法");
        return value;
    }

    private FileTransferFileVO describe(Path path) throws IOException {
        return new FileTransferFileVO(path.getFileName().toString(), Files.size(path), Files.getLastModifiedTime(path).toInstant());
    }

    private FileTransferFileVO describeUnchecked(Path path) {
        try { return describe(path); }
        catch (IOException exception) { throw new FileTransferStorageException("读取文件信息失败：" + path.getFileName(), exception); }
    }

    private static class FileTransferStorageException extends RuntimeException {
        FileTransferStorageException(String message, Throwable cause) { super(message, cause); }
    }
}
