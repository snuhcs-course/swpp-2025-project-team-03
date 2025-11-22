package com.example.voicetutor.annotations

/**
 * 이 어노테이션이 붙은 클래스, 함수, 또는 프로퍼티는 JaCoCo 리포트 생성에서 제외됩니다.
 * 
 * 사용 예시:
 * ```
 * @ExcludeFromJacocoGeneratedReport
 * class GeneratedCode {
 *     // 이 클래스는 JaCoCo 리포트에서 제외됩니다
 * }
 * 
 * @ExcludeFromJacocoGeneratedReport
 * fun generatedFunction() {
 *     // 이 함수는 JaCoCo 리포트에서 제외됩니다
 * }
 * ```
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FILE,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeFromJacocoGeneratedReport

