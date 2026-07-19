package com.aiprovider.service;

import com.aiprovider.model.vo.FileTransferDownload;
import com.aiprovider.model.vo.FileTransferFileVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileTransferServiceTest {
    @TempDir Path directory;

    @Test void uploadsListsDownloadsOverwritesAndDeletesOriginalFileName() throws Exception {
        FileTransferService service = new FileTransferService(directory.toString());
        service.upload(new MockMultipartFile("file", "设备文件.txt", "text/plain", "first".getBytes(StandardCharsets.UTF_8)));
        service.upload(new MockMultipartFile("file", "设备文件.txt", "text/plain", "second".getBytes(StandardCharsets.UTF_8)));

        List<FileTransferFileVO> files = service.list();
        assertThat(files).singleElement().satisfies(file -> {
            assertThat(file.getFileName()).isEqualTo("设备文件.txt");
            assertThat(file.getFileSize()).isEqualTo(6);
            assertThat(file.getUploadedAt()).isNotNull();
        });
        FileTransferDownload download = service.download("设备文件.txt");
        assertThat(download.getFileSize()).isEqualTo(6);
        assertThat(download.getResource().getInputStream()).hasContent("second");

        service.delete("设备文件.txt");
        assertThat(service.list()).isEmpty();
    }

    @Test void rejectsPathsOutsideConfiguredStorageDirectory() {
        FileTransferService service = new FileTransferService(directory.toString());
        assertThatThrownBy(() -> service.upload(new MockMultipartFile("file", "../outside.txt", "text/plain", new byte[0])))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("文件名不合法");
        assertThat(Files.exists(directory.getParent().resolve("outside.txt"))).isFalse();
    }
}
