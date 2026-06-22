package com.daycare.alrimjang.domain.parentmemo;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import com.daycare.alrimjang.global.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentMemoService {

    private final ParentMemoRepository parentMemoRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 학부모 메모 저장 (작성 또는 수정)
    @Transactional
    public void saveMemo(String email, LocalDate date, String content, MultipartFile[] photos) throws IOException {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Child child = childRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("연결된 원아가 없습니다."));

        ParentMemo memo = getMemo(child.getId(), date);

        if (memo != null) {
            memo.update(content);
        } else {
            memo = ParentMemo.builder()
                    .date(date)
                    .content(content)
                    .child(child)
                    .parent(parent)
                    .build();
            parentMemoRepository.save(memo);
        }

        // 사진 저장
        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    String fileName = FileUploadUtils.store(photo, System.getProperty("user.dir"), uploadDir);

                    ParentMemoPhoto memoPhoto = ParentMemoPhoto.builder()
                            .filePath(fileName)
                            .parentMemo(memo)
                            .build();
                    memo.getPhotos().add(memoPhoto);
                }
            }
        }
    }

    // 원아 + 날짜로 메모 조회 (최신 1개)
    @Transactional(readOnly = true)
    public ParentMemo getMemo(Long childId, LocalDate date) {
        List<ParentMemo> memos = parentMemoRepository.findAllByChildIdAndDate(childId, date);
        return memos.isEmpty() ? null : memos.get(memos.size() - 1);
    }
}