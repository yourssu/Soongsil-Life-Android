@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yourssu.soongsil.screen.coursecatalog

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.coursecatalog.CourseCatalogCategoryData
import com.yourssu.data.coursecatalog.CourseCatalogCourseData
import com.yourssu.data.coursecatalog.CourseCatalogData
import com.yourssu.data.coursecatalog.CourseCatalogFilterData
import com.yourssu.data.coursecatalog.CourseCatalogFilterOptionData
import com.yourssu.data.coursecatalog.CourseCatalogSelectedFilterData
import com.yourssu.data.coursecatalog.CourseCatalogSemester
import com.yourssu.soongsil.ui.components.CourseDetailBottomSheet
import com.yourssu.soongsil.ui.components.LocalMainBottomBarPadding
import com.yourssu.soongsil.ui.theme.PretendardFontFamily
import com.yourssu.soongsil.ui.theme.SoongsilLifeAndroidTheme
import com.yourssu.soongsil.ui.theme.SoongsilPalette

@Composable
fun CourseCatalogScreen(
    uiState: CourseCatalogUiState,
    onBackClick: () -> Unit,
    onYearChange: (String) -> Unit,
    onSemesterChange: (CourseCatalogSemester) -> Unit,
    onCategoryChange: (CourseCatalogCategoryData) -> Unit,
    onFilterSelect: (Int, CourseCatalogFilterOptionData) -> Unit,
    onKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRetryOptions: () -> Unit,
    onRefresh: () -> Unit,
    onPlanClick: (CourseCatalogCourseData) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalMainBottomBarPadding.current
    var selectedCourse by remember { mutableStateOf<CourseCatalogCourseData?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        CourseCatalogHeader(onBackClick = onBackClick)

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CourseCatalogContent(
                uiState = uiState,
                onYearChange = onYearChange,
                onSemesterChange = onSemesterChange,
                onCategoryChange = onCategoryChange,
                onFilterSelect = onFilterSelect,
                onKeywordChange = onKeywordChange,
                onSearchClick = onSearchClick,
                onRetryOptions = onRetryOptions,
                onCourseClick = { selectedCourse = it },
                bottomPadding = bottomBarPadding
            )
        }
    }

    selectedCourse?.let { course ->
        CourseDetailBottomSheet(
            subjectName = course.subjectName,
            classification = course.primaryClassification,
            professor = course.professor,
            countLabel = "잔여 인원",
            count = course.remainingSeats,
            details = course.toDetailItems(),
            onDismissRequest = { selectedCourse = null },
            onPlanClick = {
                selectedCourse = null
                onPlanClick(course)
            }
        )
    }
}

@Composable
private fun CourseCatalogHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "강의시간표 조회",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PretendardFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun CourseCatalogContent(
    uiState: CourseCatalogUiState,
    onYearChange: (String) -> Unit,
    onSemesterChange: (CourseCatalogSemester) -> Unit,
    onCategoryChange: (CourseCatalogCategoryData) -> Unit,
    onFilterSelect: (Int, CourseCatalogFilterOptionData) -> Unit,
    onKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRetryOptions: () -> Unit,
    onCourseClick: (CourseCatalogCourseData) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 20.dp + bottomPadding
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CourseCatalogSearchCard(
                uiState = uiState,
                onYearChange = onYearChange,
                onSemesterChange = onSemesterChange,
                onCategoryChange = onCategoryChange,
                onFilterSelect = onFilterSelect,
                onKeywordChange = onKeywordChange,
                onRetryOptions = onRetryOptions,
                onSearchClick = {
                    focusManager.clearFocus()
                    onSearchClick()
                },
                focusManager = focusManager,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        if (uiState.errorMessage != null) {
            item {
                CourseCatalogMessageCard(
                    message = uiState.errorMessage,
                    buttonText = "다시 조회",
                    onButtonClick = onSearchClick,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
        }

        when {
            uiState.isLoading -> item {
                CourseCatalogStateMessage(
                    message = "저장된 강의시간표를 확인하는 중이에요.",
                    showProgress = true,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                )
            }

            uiState.data == null -> item {
                CourseCatalogStateMessage(
                    message = "학기와 검색 조건을 입력해 강의를 조회해 보세요.",
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                )
            }

            else -> {
                item {
                    CourseCatalogResultHeader(
                        data = uiState.data,
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                if (uiState.data.courses.isEmpty()) {
                    item {
                        CourseCatalogStateMessage(
                            message = "조건에 맞는 강의가 없어요.",
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    itemsIndexed(
                        items = uiState.data.courses,
                        key = { index, course ->
                            "${course.year}-${course.semester}-${course.subjectCode}-${course.section}-$index"
                        }
                    ) { index, course ->
                        Column {
                            CourseCatalogListItem(
                                course = course,
                                onClick = { onCourseClick(course) },
                                isFirst = index == 0,
                                isLast = index == uiState.data.courses.lastIndex,
                                modifier = Modifier
                                    .widthIn(max = 480.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCatalogSearchCard(
    uiState: CourseCatalogUiState,
    onYearChange: (String) -> Unit,
    onSemesterChange: (CourseCatalogSemester) -> Unit,
    onCategoryChange: (CourseCatalogCategoryData) -> Unit,
    onFilterSelect: (Int, CourseCatalogFilterOptionData) -> Unit,
    onKeywordChange: (String) -> Unit,
    onRetryOptions: () -> Unit,
    onSearchClick: () -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var semesterMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val hasRequiredScopeSelection = !uiState.category.requiresScopeSelection ||
        !uiState.filters.firstOrNull()?.selectedKey.isNullOrBlank()
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .shadow(2.dp, cardShape)
            .fillMaxWidth(),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourseCatalogYearField(
                    year = uiState.year,
                    onYearChange = onYearChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(62.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "학기",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PretendardFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CourseCatalogSelectionField(
                            value = uiState.semester.displayName,
                            onClick = { semesterMenuExpanded = true },
                            contentDescription = "학기 선택",
                            modifier = Modifier.fillMaxWidth(),
                            showContainer = false
                        )
                        DropdownMenu(
                            expanded = semesterMenuExpanded,
                            onDismissRequest = { semesterMenuExpanded = false }
                        ) {
                            CourseCatalogSemester.entries.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = item.displayName) },
                                    onClick = {
                                        onSemesterChange(item)
                                        semesterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            CourseCatalogCategoryDropdown(
                category = uiState.category,
                onCategoryChange = onCategoryChange
            )

            uiState.filters.forEach { filter ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                CourseCatalogFilterDropdown(
                    filter = filter,
                    enabled = !uiState.isOptionsLoading,
                    onOptionSelect = { option -> onFilterSelect(filter.index, option) }
                )
            }

            if (uiState.acceptsKeyword) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                CourseCatalogKeywordField(
                    label = uiState.category.keywordLabel,
                    value = uiState.keyword,
                    onValueChange = onKeywordChange,
                    onSearch = {
                        focusManager.clearFocus()
                        onSearchClick()
                    }
                )
            }

            if (uiState.isOptionsLoading) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = "검색 조건을 불러오는 중이에요.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp
                    )
                }
            }

            uiState.optionsErrorMessage?.let { errorMessage ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = onRetryOptions) {
                        Text(text = "다시 불러오기")
                    }
                }
            }

            if (!hasRequiredScopeSelection) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "전체 강좌를 한 번에 처리하면 앱이 느려질 수 있어요. " +
                        "${uiState.filters.firstOrNull()?.name ?: "검색 조건"}을 선택해 주세요.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = PretendardFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Button(
                onClick = onSearchClick,
                enabled = !uiState.isSearching &&
                    !uiState.isOptionsLoading &&
                    hasRequiredScopeSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(51.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = if (uiState.isSearching) "조회 중" else "조회",
                    fontFamily = PretendardFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CourseCatalogYearField(
    year: String,
    onYearChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Material 입력 필드의 최소 높이에 의존하지 않아 낮은 높이에서도 글자가 잘리지 않습니다.
    Column(modifier = modifier) {
        Text(
            text = "학년도",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PretendardFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        BasicTextField(
            value = year,
            onValueChange = onYearChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = PretendardFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) { innerTextField() }
            }
        )
    }
}

@Composable
private fun CourseCatalogCategoryDropdown(
    category: CourseCatalogCategoryData,
    onCategoryChange: (CourseCatalogCategoryData) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    CourseCatalogDropdownRow(
        label = "조회 유형",
        value = category.displayName,
        menuExpanded = menuExpanded,
        onExpandedChange = { menuExpanded = it },
        contentDescription = "조회 유형 선택",
        modifier = modifier
    ) {
        CourseCatalogCategoryData.entries.forEach { item ->
            DropdownMenuItem(
                text = { Text(text = item.displayName) },
                onClick = {
                    onCategoryChange(item)
                    menuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun CourseCatalogFilterDropdown(
    filter: CourseCatalogFilterData,
    enabled: Boolean,
    onOptionSelect: (CourseCatalogFilterOptionData) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by rememberSaveable(filter.index) { mutableStateOf(false) }

    CourseCatalogDropdownRow(
        label = filter.name,
        value = filter.selectedLabel.ifBlank { "${filter.name} 선택" },
        enabled = enabled && filter.options.isNotEmpty(),
        menuExpanded = menuExpanded,
        onExpandedChange = { menuExpanded = it },
        contentDescription = "${filter.name} 선택",
        modifier = modifier
    ) {
        filter.options.forEach { option ->
            DropdownMenuItem(
                text = { Text(text = option.label.ifBlank { "전체" }) },
                onClick = {
                    onOptionSelect(option)
                    menuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun CourseCatalogDropdownRow(
    label: String,
    value: String,
    menuExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(78.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PretendardFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.weight(1f)) {
            CourseCatalogSelectionField(
                value = value,
                onClick = { onExpandedChange(true) },
                contentDescription = contentDescription,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                menuContent()
            }
        }
    }
}

@Composable
private fun CourseCatalogSelectionField(
    value: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showContainer: Boolean = true
) {
    // 선택 필드의 텍스트 기준선을 직접 맞춰 39dp 디자인 높이를 안정적으로 유지합니다.
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(39.dp)
            .then(
                if (showContainer) {
                    Modifier
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (showContainer) 12.dp else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontFamily = PretendardFontFamily,
            fontSize = if (showContainer) 14.sp else 16.sp,
            fontWeight = if (showContainer) FontWeight.SemiBold else FontWeight.Bold,
            lineHeight = if (showContainer) 21.sp else 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CourseCatalogKeywordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(78.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = PretendardFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        val shape = RoundedCornerShape(8.dp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(39.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .padding(horizontal = 12.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = PretendardFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text(
                                text = "$label 입력",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = PretendardFontFamily,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun CourseCatalogResultHeader(
    data: CourseCatalogData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${data.year}학년도 ${data.semester.displayName}",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = PretendardFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = buildList {
                    add(data.category.displayName)
                    addAll(data.selectedFilters.map { it.label }.filter(String::isNotBlank))
                    data.keyword.takeIf(String::isNotBlank)?.let(::add)
                }.joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PretendardFontFamily,
                fontSize = 12.sp
            )
        }
        Text(
            text = "총 ${data.totalCourseCount}개",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = PretendardFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CourseCatalogListItem(
    course: CourseCatalogCourseData,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }
    val remainingCount = course.remainingSeats.replace(",", "").toIntOrNull()
    val remainingColor = when {
        remainingCount == null -> MaterialTheme.colorScheme.onSurfaceVariant
        remainingCount == 0 -> MaterialTheme.colorScheme.outlineVariant
        remainingCount <= 10 -> SoongsilPalette.Orange500
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = course.subjectName.orDash(),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = PretendardFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOf(course.primaryClassification, course.professor)
                    .filter(String::isNotBlank)
                    .joinToString(" · ")
                    .orDash(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PretendardFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // 수강 대상은 가운데 남는 폭을 모두 사용하고, 긴 내용은 두 줄까지 표시합니다.
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = course.targetStudents.orDash(),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = PretendardFontFamily,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "수강 대상",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PretendardFontFamily,
                fontSize = 10.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = course.remainingSeats.orDash(),
                color = remainingColor,
                fontFamily = PretendardFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "잔여 인원",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PretendardFontFamily,
                fontSize = 10.sp
            )
        }
    }
}

private fun CourseCatalogCourseData.toDetailItems(): List<Pair<String, String>> = listOf(
    "수업시간" to schedule,
    "시간 / 학점" to hoursCredits,
    "강의계획서 정보" to plan,
    "다전공 이수구분" to multiMajorClassification,
    "공학인증" to engineeringCertification,
    "교과영역" to curriculumArea,
    "과목번호" to subjectCode,
    "수강신청 유의사항" to registrationNotice,
    "강의 유형" to courseType,
    "분반" to section,
    "개설 학과" to department,
    "수강 정원" to enrollmentCapacity,
    "잔여 인원" to remainingSeats,
    "수강 대상" to targetStudents,
    "학년도" to year,
    "학기" to semester.displayName
)

@Composable
private fun CourseCatalogMessageCard(
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Button(onClick = onButtonClick) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun CourseCatalogStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
            }
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private val CourseCatalogCategoryData.keywordLabel: String
    get() = when (this) {
        CourseCatalogCategoryData.PROFESSOR -> "교수명"
        else -> "과목명 / 과목번호"
    }

private fun String.orDash(): String = ifBlank { "-" }

private val previewCourseCatalogFilters = listOf(
    CourseCatalogFilterData(
        index = 0,
        name = "대학",
        selectedKey = "11000037",
        selectedLabel = "IT대학",
        options = listOf(
            CourseCatalogFilterOptionData("", "전체"),
            CourseCatalogFilterOptionData("11000037", "IT대학")
        )
    ),
    CourseCatalogFilterData(
        index = 1,
        name = "학부/학과",
        selectedKey = "11000038",
        selectedLabel = "컴퓨터학부",
        options = listOf(
            CourseCatalogFilterOptionData("", "전체"),
            CourseCatalogFilterOptionData("11000038", "컴퓨터학부")
        )
    ),
    CourseCatalogFilterData(
        index = 2,
        name = "전공",
        options = listOf(CourseCatalogFilterOptionData("", "전체"))
    )
)

private val previewCourseCatalogData = CourseCatalogData(
    year = "2026",
    semester = CourseCatalogSemester.SECOND,
    category = CourseCatalogCategoryData.DEPARTMENT,
    filterKeys = listOf("11000037", "11000038"),
    selectedFilters = listOf(
        CourseCatalogSelectedFilterData("대학", "11000037", "IT대학"),
        CourseCatalogSelectedFilterData("학부/학과", "11000038", "컴퓨터학부")
    ),
    totalCourseCount = 1,
    courses = listOf(
        CourseCatalogCourseData(
            plan = "강의계획서 제공",
            primaryClassification = "전공선택",
            multiMajorClassification = "복수전공선택",
            engineeringCertification = "전문교양",
            curriculumArea = "전공",
            subjectCode = "21500123",
            subjectName = "모바일프로그래밍",
            registrationNotice = "컴퓨터학부 학생을 대상으로 합니다.",
            courseType = "이론/실습",
            section = "01",
            professor = "김교수",
            department = "컴퓨터학부",
            hoursCredits = "3 / 3",
            enrollmentCapacity = "40",
            remainingSeats = "12",
            schedule = "월 10:30-11:45, 수 10:30-11:45",
            targetStudents = "컴퓨터학부 3·4학년 및 복수전공 학생",
            year = "2026",
            semester = CourseCatalogSemester.SECOND
        )
    )
)

@Preview(name = "조회 결과 - 라이트", showBackground = true, heightDp = 900)
@Preview(
    name = "조회 결과 - 다크",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CourseCatalogScreenPreview() {
    SoongsilLifeAndroidTheme {
        CourseCatalogScreen(
            uiState = CourseCatalogUiState(
                data = previewCourseCatalogData,
                filters = previewCourseCatalogFilters,
                isLoading = false
            ),
            onBackClick = {},
            onYearChange = {},
            onSemesterChange = {},
            onCategoryChange = {},
            onFilterSelect = { _, _ -> },
            onKeywordChange = {},
            onSearchClick = {},
            onRetryOptions = {},
            onRefresh = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "상세 바텀시트 - 라이트", showBackground = true, heightDp = 900)
@Preview(
    name = "상세 바텀시트 - 다크",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CourseCatalogBottomSheetPreview() {
    SoongsilLifeAndroidTheme {
        val course = previewCourseCatalogData.courses.first()
        CourseDetailBottomSheet(
            subjectName = course.subjectName,
            classification = course.primaryClassification,
            professor = course.professor,
            countLabel = "잔여 인원",
            count = course.remainingSeats,
            details = course.toDetailItems(),
            onDismissRequest = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "초기 상태 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "초기 상태 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CourseCatalogEmptyPreview() {
    SoongsilLifeAndroidTheme {
        CourseCatalogScreen(
            uiState = CourseCatalogUiState(isLoading = false),
            onBackClick = {},
            onYearChange = {},
            onSemesterChange = {},
            onCategoryChange = {},
            onFilterSelect = { _, _ -> },
            onKeywordChange = {},
            onSearchClick = {},
            onRetryOptions = {},
            onRefresh = {},
            onPlanClick = {}
        )
    }
}

@Preview(name = "검색 조건 로딩 - 라이트", showBackground = true, heightDp = 800)
@Preview(
    name = "검색 조건 로딩 - 다크",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CourseCatalogOptionsLoadingPreview() {
    SoongsilLifeAndroidTheme {
        CourseCatalogScreen(
            uiState = CourseCatalogUiState(
                filters = previewCourseCatalogFilters,
                isLoading = false,
                isOptionsLoading = true
            ),
            onBackClick = {},
            onYearChange = {},
            onSemesterChange = {},
            onCategoryChange = {},
            onFilterSelect = { _, _ -> },
            onKeywordChange = {},
            onSearchClick = {},
            onRetryOptions = {},
            onRefresh = {},
            onPlanClick = {}
        )
    }
}
