@file:OptIn(UiToolingDataApi::class)
package com.zachklipp.richtext.ui.printing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.NodeGroup
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.tooling.data.asTree
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable private fun PagedPreview() {
  Paged(
    modifier = Modifier
      .height(100.dp)
      .width(100.dp),
    drawBreakpoints = true
  ) {
    Column {
      for (i in 0 until 100) {
        Text("$i", style = TextStyle(fontSize = 32.sp))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable private fun PagedPreviewPage2() {
  PagedImpl(
    modifier = Modifier
      .height(100.dp)
      .width(100.dp),
    drawBreakpoints = true,
    pageOffsetPx = 20
  ) {
    Column {
      for (i in 0 until 100) {
        Text("$i", style = TextStyle(fontSize = 32.sp))
      }
    }
  }
}

/**
 * Represents a point in a composition where it is safe to break the page.
 *
 * @param xAnchorPx The start and end x coordinates of the composable that triggered the breakpoint.
 * @param yPx The y coordinate of the breakpoint, relative to the [Paged] composable (not affected
 * by page offset).
 * @param forceBreak If false, represents point where the page may break if necessary, but may not.
 * If true, represents a point where the page will always break. Note this is not actually
 * implemented yet.
 */
@Immutable
public data class PageBreakpoint(
  val xAnchorPx: Pair<Int, Int>,
  val yPx: Int,
  val forceBreak: Boolean
)

/**
 * Returns a [Modifier] that will prevent [Paged] from putting a page break between this composable
 * and the next.
 *
 * This modifier must come before other layout modifiers, or it will have no effect.
 */
public fun Modifier.keepOnPageWithNext(): Modifier = this.then(KeepWithNextModifier)

private object KeepWithNextModifier : Modifier.Element, ParentDataModifier {
  override fun Density.modifyParentData(parentData: Any?): Any = this
}

/**
 * After the first frame of a [Paged] is committed, the breakpoints and page offsets are calculated
 * and passed to a callback via an instance of this interface.
 */
@Immutable
public interface PageLayoutResult {
  /** Page size in pixels. */
  public val pageSizePx: IntSize

  /**
   * The pixel offsets of each page, for passing to [Paged]'s `pageOffsetPx` parameter.
   * The last offset will be the end of the last page.
   */
  public val pageOffsetsPx: List<Int>
  public val breakpoints: List<PageBreakpoint>

  /**
   * Given the offset of the start of the current page, returns the y offset in pixels of the start
   * of the next page. In other words, the offset of the last breakpoint that would fit on the
   * current page but get clipped.
   */
  public fun nextPageOffsetPx(currentPageOffsetPx: Int): Int?
}

/**
 * Divides a composable into discreet pages. The [content] is measured with unbounded height, and
 * then displayed using the current constraints, starting at [pageIndex] (the content is
 * translated up). The bottom of the [content] is clipped, but a best-effort attempt is made to
 * not cut any individual composables off in the middle. When the initial "pagination" measurement
 * is complete, [onPageLayout] is invoked with the total page count.
 * ## Pagination
 *
 * In order to calculate where to clip each page, this composable uses the Compose tooling library
 * to analyze the entire slot table. It collects all the leaf `LayoutNode`s (those without children)
 * and reports their bounds. The nodes are then sorted by their bottom bound, and nodes that are
 * overlapped are removed (the bottom-most composables are kept). The remaining nodes bounds are
 * returned as [PageBreakpoint]s in a [PageLayoutResult]. The [Modifier.keepOnSamePage] modifier
 * can be used to keep a composable's children together.
 *
 * The [PageLayoutResult] then iterates through all the breakpoints and determines the offsets of
 * the start of each page (see [PageLayoutResult.pageOffsetsPx]).
 *
 * There are some known issues with the current implementation:
 *  - Only the slot table for the current composition is analyzed. Any children which use
 *    subcomposition (e.g. `WithConstraints`, `LazyColumn`) will be considered as a "leaf"
 *    composable.
 *  - Large text blocks are treated as a single unit, individual lines will not be broken across
 *    pages.
 *  - Nested [Paged] composables are not supported (behavior is undefined).
 *
 * @param modifier A [Modifier] that will always be applied to [content].
 * @param pageIndex The index of the page to render. Valid values are between 0 and the value passed
 * to the [onPageLayout] callback.
 * @param pageModifier A [Modifier] that is applied to each page, and not affected by pagination.
 * @param clipLastBreakpoint If false, the content at the end of the current page will be clipped
 * exactly at the page bounds, not at the nearest breakpoint. True by default.
 * @param drawBreakpoints If true, horizontal lines are drawn at each breakpoint for debugging.
 * False by default.
 * @param onPageLayout Callback that will be invoked after calculating the total number of pages.
 */
@Composable public fun Paged(
  modifier: Modifier = Modifier,
  pageIndex: Int = 0,
  pageModifier: Modifier = Modifier,
  clipLastBreakpoint: Boolean = true,
  drawBreakpoints: Boolean = false,
  onPageLayout: ((pageCount: Int) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  var layoutResult: PageLayoutResult? by remember { mutableStateOf(null) }

  val pageOffsetPx = layoutResult?.let {
    it.pageOffsetsPx[pageIndex.coerceIn(0, it.pageOffsetsPx.size - 1)]
  } ?: 0

  PagedImpl(
    modifier = modifier,
    pageOffsetPx = pageOffsetPx,
    pageModifier = pageModifier,
    clipLastBreakpoint = clipLastBreakpoint,
    drawBreakpoints = drawBreakpoints,
    onPageLayoutResult = {
      layoutResult = it
      onPageLayout?.invoke(it.pageOffsetsPx.size)
    },
    content = content
  )
}

/**
 * Divides a composable into discreet pages. The [content] is measured with unbounded height, and
 * then displayed using the current constraints, starting at [pageOffsetPx] (the content is
 * translated up). The bottom of the [content] is clipped, but a best-effort attempt is made to
 * not cut any individual composables off in the middle. When the initial "pagination" measurement
 * is complete, [onPageLayoutResult] is invoked with a [PageLayoutResult] that can be used to
 * get the offsets of each page to pass to [pageOffsetPx].
 *
 * @param pageOffsetPx The y offset to shift the composable up by.
 * See [PageLayoutResult.pageOffsetsPx] and [PageLayoutResult.nextPageOffsetPx].
 * @param pageModifier A [Modifier] that is applied to each page, and not affected by pagination.
 * @param clipLastBreakpoint If false, the content at the end of the current page will be clipped
 * exactly at the page bounds, not at the nearest breakpoint. True by default.
 * @param drawBreakpoints If true, horizontal lines are drawn at each breakpoint for debugging.
 * False by default.
 */
@Composable private fun PagedImpl(
  modifier: Modifier = Modifier,
  pageOffsetPx: Int = 0,
  pageModifier: Modifier = Modifier,
  clipLastBreakpoint: Boolean = true,
  drawBreakpoints: Boolean = false,
  onPageLayoutResult: ((PageLayoutResult) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  require(pageOffsetPx >= 0) { "pageOffsetPx must be non-negative, but was $pageOffsetPx" }

  var coordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
  var pageLayoutResult: PageLayoutResult? by remember { mutableStateOf(null) }
  var measureModifier = modifier
    .onGloballyPositioned { coordinates = it }
    .then(pageModifier)

  measureModifier = measureModifier.drawWithContent {
    val bottomClipAbsolute = pageLayoutResult?.takeIf { clipLastBreakpoint }
      ?.nextPageOffsetPx(pageOffsetPx)
    val bottomClip = bottomClipAbsolute
      ?.let { it - pageOffsetPx }
      ?: size.height.toInt()

    clipRect(bottom = bottomClip.toFloat()) {
      translate(top = -pageOffsetPx.toFloat()) {
        this@drawWithContent.drawContent()

        if (drawBreakpoints) {
          pageLayoutResult?.let { drawBreakpoints(it, bottomClipAbsolute) }
        }
      }
    }
  }

  Box(measureModifier) {
    MeasureBreakpoints(
      onBreakpoints = { globalBreakpoints ->
        coordinates?.let { coords ->
          val localBreakpoints = globalBreakpoints.map { breakpointGlobalBounds ->
            val localRect = breakpointGlobalBounds.translate(-coords.positionInWindow().round())
            PageBreakpoint(
              xAnchorPx = Pair(localRect.left, localRect.right),
              yPx = localRect.bottom,
              forceBreak = false
            )
          }

          pageLayoutResult = PageLayoutResultImpl(coords.size, localBreakpoints)
            .also { onPageLayoutResult?.invoke(it) }
        }
      },
      content
    )
  }
}

@Immutable
private data class PageLayoutResultImpl(
  override val pageSizePx: IntSize,
  override val breakpoints: List<PageBreakpoint>
) : PageLayoutResult {
  override val pageOffsetsPx: List<Int> = calculatePageOffsets()

  override fun nextPageOffsetPx(currentPageOffsetPx: Int): Int? {
    if (breakpoints.isEmpty()) return null

    val pageBottom = currentPageOffsetPx + pageSizePx.height
    // TODO support hard breaks.
    var firstBreakpoint: PageBreakpoint? = null
    var lastBreakpoint: PageBreakpoint? = null

    for (i in 0 until breakpoints.size - 1) {
      val breakpoint = breakpoints[i]
      val nextBreakpoint = breakpoints[i + 1]
      if (firstBreakpoint == null && breakpoint.yPx >= currentPageOffsetPx) {
        firstBreakpoint = breakpoint
      }
      if (nextBreakpoint.yPx > pageBottom) {
        lastBreakpoint = breakpoint
        break
      }
    }

    if (firstBreakpoint == null) {
      // Offset is past the last breakpoint.
      return null
    }

    if (firstBreakpoint === lastBreakpoint) {
      // Current page doesn't have a single breakpoint in it, so the page will be clipped, and the
      // next page will start also clipped.
      return pageBottom
    }

    // List is not empty, so lastBreakpoint will never be null.
    return lastBreakpoint?.yPx
  }

  private fun calculatePageOffsets(): List<Int> {
    if (breakpoints.isEmpty()) return emptyList()
    // TODO This has O(n!) time complexity, can optimize if we can tell nextPageOffsetPx where to
    //  start from on each iteration.
    return generateSequence(0) { nextPageOffsetPx(it) }
      .toList()
  }
}

private fun DrawScope.drawBreakpoints(
  layout: PageLayoutResult,
  bottomClipAbsolute: Int?
) {
  layout.breakpoints.forEach { breakpoint ->
    val y = breakpoint.yPx.toFloat()
    val x1 = breakpoint.xAnchorPx.first.toFloat()
    val x2 = breakpoint.xAnchorPx.second.toFloat()
    drawLine(
      color = Color.Red,
      strokeWidth = 1f,
      alpha = .3f,
      start = Offset(0f, y),
      end = Offset(size.width, y)
    )
    drawLine(
      color = Color.Red,
      strokeWidth = 1f,
      start = Offset(x1, y),
      end = Offset(x2, y)
    )
  }

  bottomClipAbsolute?.let {
    drawLine(
      color = Color.Red,
      strokeWidth = 1.5f,
      start = Offset(0f, it.toFloat()),
      end = Offset(size.width, it.toFloat())
    )
  }
}

/**
 * @param onBreakpoints Callback that will be invoked with a list of all bottom y-coordinates of all
 * composables in the current composition (not just children of [content]). Coordinates are global.
 */
@OptIn(InternalComposeApi::class)
@Composable private fun MeasureBreakpoints(
  onBreakpoints: (List<IntRect>) -> Unit,
  content: @Composable () -> Unit
) {
  val rootNodeMarker = remember { Modifier.layoutId(Unit) }

  // Lays children out stacked vertically and measured with unbounded height, but with reported
  // height constrained to incoming constraints, like ScrollableColumn (but not scrollable).
  Layout(modifier = rootNodeMarker, content = content) { measurables, constraints ->
    // We need to measure with unbounded height, but still report an appropriate (constrained)
    // height, so we don't get centered.
    val childConstraints = constraints.copy(maxHeight = Int.MAX_VALUE)
    val placeables = measurables.map { it.measure(childConstraints) }
    val maxChildWidth = placeables.maxOfOrNull { it.width } ?: 0
    val totalChildHeight = placeables.sumBy { it.height }

    val actualWidth = constraints.constrainWidth(maxChildWidth)
    val actualHeight = constraints.constrainHeight(totalChildHeight)
    layout(actualWidth, actualHeight) {
      // Stack vertically.
      var y = 0
      placeables.forEach {
        it.placeRelative(0, y)
        y += it.height
      }
    }
  }

  val composer = currentComposer
  // We need to wait for the positions to settle before reading the slot table.
  // The coroutine started by launchInComposition won't start right away, it will wait until the
  // frame is committed, and then dispatch, so this provides the necessary delay.
  LaunchedEffect(Unit) {
    // Read the slot table on the main thread, but calculate everything else in the background.
    val rootGroup = composer.compositionData.asTree()
    val breakpoints = mutableListOf<IntRect>()
    calculateBreakpoints(rootGroup, rootModifier = rootNodeMarker, breakpoints)
    onBreakpoints(breakpoints)
  }
}

/**
 * @param group The root [Group] to start scanning from.
 * @param rootModifier The [Modifier] that identifies the subtree of LayoutNodes to actually scan.
 * Other nodes outside of this subtree will be ignored.
 */
private fun calculateBreakpoints(
  group: Group,
  rootModifier: Modifier,
  breakpoints: MutableList<IntRect>
) {
  if (group.modifierInfo.any { it.modifier === rootModifier }) {
    group.getNodeBounds().forEach {
      calculateBreakpoints(it, breakpoints)
    }
    return
  }

  group.children.forEach {
    calculateBreakpoints(it, rootModifier, breakpoints)
  }
}

private fun calculateBreakpoints(
  nodeBounds: NodeBounds,
  breakpoints: MutableList<IntRect>
) {
  // Algorithm:
  // 1. Sort by bottom bound.
  // 2. Remove all bounds that are overlapped by check each element to see if the next one overlaps
  //    it.

  val leaves = nodeBounds.leaves.toMutableList()
  leaves.sortBy { it.bounds.bottom }

  var index = 0
  while (index < leaves.size) {
    val current = leaves[index]
    val next = if (index < leaves.size - 1) leaves[index + 1] else null
    if (next != null && current.bounds.bottom > next.bounds.top) {
      // Next node overlaps current node, so this node can't be a breakpoint.
      leaves.removeFirst()
      // The same index will now point to the next node, so don't increment index.
    } else {
      breakpoints += current.bounds
      index++
    }
  }
}

private val NodeBounds.leaves: Sequence<NodeBounds>
  get() = if (children.isEmpty()) {
    sequenceOf(this)
  } else {
    children.flatMap { it.leaves }
  }

@Immutable
private data class NodeBounds(
  val bounds: IntRect,
  val group: NodeGroup,
  val children: Sequence<NodeBounds>
)

private fun Group.getNodeBounds(): Sequence<NodeBounds> {
  val children = children.asSequence()
    .flatMap { it.getNodeBounds() }
    // TODO Filtering out keep-with-next nodes prevents page breaks after them, but also prevents
    //  them from being included in overlap calculation. Figure out a better way.
    .filterNot { it.group.keepWithNext }

  return if (this is NodeGroup) {
    sequenceOf(NodeBounds(box, this, children))
  } else {
    children
  }
}

private fun Sequence<*>.isEmpty() = !iterator().hasNext()

private val Group.keepWithNext: Boolean
  get() = modifierInfo.any { it.modifier is KeepWithNextModifier }
