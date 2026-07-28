# screen/graduation

루트 [AGENTS.md](../../../../../../../../../AGENTS.md) 규칙을 그대로 따르며, 이 패키지에서 추가로 지키는 규칙입니다.

- LMS-API의 `GraduateTableCell`을 화면/ViewModel에 그대로 노출하지 않고,
  `data` 모듈의 `GraduationRequirementItem` / `GraduationData`로 변환해서 사용합니다.
- 이수 결과 판정 문자열은 `"충족"`, `"합격"`, `"가능"`을 통과로 간주합니다.
  (`GraduationScreen.kt`의 `isPassResult()` 참고)
- 기준학점/계산학점이 없는 항목(예: 졸업논문 이수 여부)은 서브텍스트를 표시하지 않습니다.
