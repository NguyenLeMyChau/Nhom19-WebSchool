package vn.edu.iuh.fit.core.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.core.dto.ClassDTO;
import vn.edu.iuh.fit.core.dto.RegisteredDTO;
import vn.edu.iuh.fit.core.dto.SubjectDTO;
import vn.edu.iuh.fit.core.models.Class;
import vn.edu.iuh.fit.core.models.Response;
import vn.edu.iuh.fit.core.models.Semester;
import vn.edu.iuh.fit.core.services.CourseServices;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseServices courseServices;

    @GetMapping("/current-semester")
    public ResponseEntity<Semester> getCurrentSemesters() {
        Semester semester = courseServices.getCurrentSemesters();
        return ResponseEntity.ok(semester);
    }

    @GetMapping("/semesters-in-range")
    public ResponseEntity<Response> getSemestersInRange(@RequestParam String course) {
        List<Semester> semesters = courseServices.getSemestersInRange(course);
        Response response = new Response(200, "Success", semesters);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}/subjects")
    public List<SubjectDTO> getSubjectsByMajorAndStudent(
            @PathVariable String studentId,
            @RequestParam Integer major) {
        List<Object[]> results = courseServices.findSubjectsByMajorAndStudent(major, studentId);
        List<SubjectDTO> subjects = new ArrayList<>();
        for (Object[] result : results) {
            String subjectId = (String) result[0];
            Integer credits = (Integer) result[1];
            String name = (String) result[2];
            Boolean status = (Boolean) result[3];
            Double tuition = (Double) result[4];
            String parentId = (String) result[5];
            subjects.add(new SubjectDTO(subjectId, credits, name, status, tuition, parentId));
        }
        return subjects;
    }

    @GetMapping("/classes/{subjectId}")
    public ResponseEntity<List<ClassDTO>> getClassesBySubject(@PathVariable String subjectId, @RequestParam int semesterId){
        List<Class> results = courseServices.findClassesBySubjectAndSemester(subjectId, semesterId);
        List<ClassDTO> classes = new ArrayList<>();
        for (Class result : results) {
            ClassDTO classDTO = new ClassDTO();
            classDTO.setId(result.getId());
            classDTO.setName(result.getName());
            classDTO.setMaxEnrollment(result.getMaxEnrollment());
            classDTO.setTeacher(result.getTeacher());
            classDTO.setDayOfWeek(result.getDayOfWeek());
            classDTO.setLesson(result.getLesson());
            classDTO.setStartDate(result.getStartDate());
            classDTO.setEndDate(result.getEndDate());
            classDTO.setClassroom(result.getClassroom());
            classes.add(classDTO);
        }
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<Integer> countStudentsInClass(@PathVariable String classId) {
        int count = courseServices.countStudentsInClass(classId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollStudentToClass(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String classId = body.get("classId");
        LocalDate regisDate = LocalDate.now();
        boolean result = courseServices.enrollStudentToClass(studentId, classId, regisDate);
        if (result) {
            return ResponseEntity.ok("Đăng ký môn học thành công");
        } else {
            return ResponseEntity.status(500).body("Đăng ký môn học thất bại");
        }
    }

    @GetMapping("/{studentId}/classes")
    public ResponseEntity<List<RegisteredDTO>> getStudentClasses(@PathVariable String studentId) {
        List<Object[]> results = courseServices.findStudentClasses(studentId);
        List<RegisteredDTO> classes = new ArrayList<>();
        for (Object[] result : results) {
            RegisteredDTO classDTO = new RegisteredDTO();
            classDTO.setClassId((String) result[0]);
            classDTO.setName((String) result[1]);
            classDTO.setCredits((Integer) result[2]);
            classDTO.setTotal((Double) result[3]);
            java.sql.Date sqlDate = (java.sql.Date) result[4];
            LocalDate localDate = sqlDate.toLocalDate();
            classDTO.setRegisDate(localDate);
            Long longValue = (Long) result[5];
            Boolean booleanValue = longValue != 0;
            classDTO.setStatus(booleanValue);
            classes.add(classDTO);
        }

        return ResponseEntity.ok(classes);
    }

    @DeleteMapping("/un-enroll")
    public ResponseEntity<String> deleteStudentFromClass(@RequestParam("studentId") String studentId, @RequestParam("classId") String classId) {
        boolean result = courseServices.deleteStudentFromClass(classId, studentId);
        if (result) {
            return ResponseEntity.ok("Hủy đăng ký môn học thành công");
        } else {
            return ResponseEntity.status(500).body("Hủy đăng ký môn học thất bại");
        }
    }
}