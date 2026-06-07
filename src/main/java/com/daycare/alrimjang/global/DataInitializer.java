package com.daycare.alrimjang.global;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.classroom.ClassroomTeacher;
import com.daycare.alrimjang.domain.classroom.ClassroomTeacherRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ChildRepository childRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // ADMIN 계정 생성
        if (!userRepository.existsByEmail("admin@daycare.com")) {
            userRepository.save(User.builder()
                    .name("원장")
                    .email("admin@daycare.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .role(User.Role.ADMIN)
                    .status(User.Status.ACTIVE)
                    .emailNotification(true)
                    .build());
            System.out.println("ADMIN 계정 생성 완료");
        }

        // 테스트용 반 생성
        Classroom classroom;
        if (classroomRepository.count() == 0) {
            classroom = classroomRepository.save(Classroom.builder()
                    .name("햇살반")
                    .build());
            System.out.println("햇살반 생성 완료");
        } else {
            classroom = classroomRepository.findAll().get(0);
        }

        // 테스트용 교사 계정 생성
        if (!userRepository.existsByEmail("teacher@daycare.com")) {
            User teacher = userRepository.save(User.builder()
                    .name("김선생")
                    .email("teacher@daycare.com")
                    .password(passwordEncoder.encode("teacher1234"))
                    .role(User.Role.TEACHER)
                    .status(User.Status.ACTIVE)
                    .emailNotification(true)
                    .build());

            // 교사 반 배정
            classroomTeacherRepository.save(ClassroomTeacher.builder()
                    .classroom(classroom)
                    .user(teacher)
                    .build());
            System.out.println("교사 계정 생성 완료");
        }

        // 테스트용 원아 생성
        if (childRepository.count() == 0) {
            childRepository.save(Child.builder()
                    .name("김하늘")
                    .classroom(classroom)
                    .build());
            childRepository.save(Child.builder()
                    .name("이민준")
                    .classroom(classroom)
                    .build());
            childRepository.save(Child.builder()
                    .name("박서연")
                    .classroom(classroom)
                    .build());
            System.out.println("테스트 원아 3명 생성 완료");
        }

// 테스트용 학부모 계정 생성
        if (!userRepository.existsByEmail("parent@daycare.com")) {
            User parent = userRepository.save(User.builder()
                    .name("김부모")
                    .email("parent@daycare.com")
                    .password(passwordEncoder.encode("parent1234"))
                    .role(User.Role.PARENT)
                    .status(User.Status.ACTIVE)
                    .emailNotification(true)
                    .build());

            // 원아 목록 다시 조회해서 첫 번째 원아와 연결
            List<Child> children = childRepository.findByClassroomId(classroom.getId());
            if (!children.isEmpty()) {
                children.get(0).assignParent(parent);
                childRepository.save(children.get(0));
            }
            System.out.println("학부모 계정 생성 완료 - 원아 연결 완료");
        }
    }
}