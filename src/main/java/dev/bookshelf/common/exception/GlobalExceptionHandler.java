package dev.bookshelf.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Mengonversi seluruh exception menjadi RFC 9457 ProblemDetail (application/problem+json).
 * Lihat API-CONTRACT.md untuk format error yang dijanjikan ke klien.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_BLANK = URI.create("about:blank");

    private ProblemDetail problem(HttpStatus status, String detail, String instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(TYPE_BLANK);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(instance));
        return pd;
    }

    /** 404 — data tidak ditemukan. */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), pathOf(ex));
    }

    /** 409 — konflik bisnis (ISBN/nama/slug duplikat, data masih dipakai). */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), pathOf(ex));
    }

    /** 400 — validasi @Valid gagal pada body. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError)
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validasi gagal", pathOf(ex));
        pd.setProperty("errors", errors);
        return pd;
    }

    private Map<String, String> fieldError(FieldError fe) {
        return Map.of(
                "field", fe.getField(),
                "message", fe.getDefaultMessage() == null ? "tidak valid" : fe.getDefaultMessage()
        );
    }

    /** 400 — validasi gagal pada parameter path/query (@Validated). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", cv.getPropertyPath().toString(),
                        "message", cv.getMessage()
                ))
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validasi gagal", pathOf(ex));
        pd.setProperty("errors", errors);
        return pd;
    }

    /** 400 — validasi manual (IllegalArgumentException) pada PATCH parsial. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validasi gagal", pathOf(ex));
        pd.setProperty("errors", List.of(Map.of("field", "body", "message", ex.getMessage())));
        return pd;
    }

    /** 400 — body JSON rusak / tidak terurai. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Body JSON tidak valid", pathOf(ex));
    }

    /** 400 — parameter dengan tipe salah (mis. ?year=abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() == null ? "?" : ex.getRequiredType().getSimpleName();
        String detail = "Parameter '%s' harus berupa %s".formatted(ex.getName(), expected);
        return problem(HttpStatus.BAD_REQUEST, detail, pathOf(ex));
    }

    /** 409 — pelanggaran constraint DB (unique/FK) yang lolos dari pengecekan service. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        String detail = "Data melanggar constraint database";
        String root = rootMessage(ex);
        if (root != null) {
            if (root.contains("uq_books_isbn")) {
                detail = "ISBN sudah terdaftar";
            } else if (root.contains("uq_publishers_name")) {
                detail = "Nama penerbit sudah terdaftar";
            } else if (root.contains("uq_categories_name") || root.contains("uq_categories_slug")) {
                detail = "Nama atau slug kategori sudah terdaftar";
            } else if (root.contains("fk_books_publisher")) {
                detail = "Penerbit tidak ditemukan";
            } else {
                detail = root;
            }
        }
        return problem(HttpStatus.CONFLICT, detail, pathOf(ex));
    }

    private String rootMessage(Throwable ex) {
        Throwable cur = ex;
        String msg = null;
        while (cur != null) {
            if (cur.getMessage() != null) msg = cur.getMessage();
            cur = cur.getCause();
        }
        return msg;
    }

    private String pathOf(Throwable ex) {
        org.springframework.web.context.request.RequestAttributes attrs =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes servlet) {
            return servlet.getRequest().getRequestURI();
        }
        return "unknown";
    }
}
