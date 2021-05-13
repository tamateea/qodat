package stan.qodat.scene.controller

import javafx.beans.property.SimpleDoubleProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Sphere
import javafx.util.Callback
import stan.qodat.Qodat
import stan.qodat.scene.SubScene3D
import stan.qodat.scene.runescape.animation.AnimationFrame
import stan.qodat.scene.runescape.model.Model
import stan.qodat.scene.runescape.animation.Transformation
import stan.qodat.util.FrameTimeUtil
import stan.qodat.util.onIndexSelected
import java.net.URL
import java.util.*

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   29/01/2021
 */
class TimeLineController : Initializable {

    @FXML lateinit var timeLineBox: HBox
    @FXML lateinit var frameList: ListView<AnimationFrame>
    @FXML lateinit var transformsList: ListView<Transformation>

    private val transformSpheres = Group()

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        HBox.setHgrow(frameList, Priority.ALWAYS)

        val animationPlayer = SubScene3D.animationPlayer

        timeLineBox.children.removeAll(transformsList)

        frameList.onIndexSelected {
            if (this >= 0) {
                if (this != animationPlayer.frameIndexProperty.get()) {
                    animationPlayer.jumpToFrame(this)
                    val frame = frameList.items[this]
                    val groupIndicesList = frame.transformationList.map {
                        it.groupIndices.toArray(null)
                    }
                    transformSpheres.children.clear()

                    animationPlayer.transformableList.forEach {

                        if (it is Model) {
                            var previousSphere: Sphere? = null
                            for (groupIndices in groupIndicesList) {
                                for (group in groupIndices) {
                                    val vertices = it.getVertexGroup(group)
                                    if (vertices.isEmpty())
                                        continue
                                    var totalX = 0
                                    var totalY = 0
                                    var totalZ = 0
                                    for (vertex in vertices) {
                                        totalX += it.getX(vertex)
                                        totalY += it.getY(vertex)
                                        totalZ += it.getZ(vertex)
                                    }
                                    val centerX = totalX / vertices.size
                                    val centerY = totalY / vertices.size
                                    val centerZ = totalZ / vertices.size
                                    val sphere = Sphere(3.0)
                                    if (previousSphere != null) {
                                        //TODO: 3d lines between spheres
                                    }
                                    previousSphere = sphere

                                    sphere.translateX = centerX.toDouble()
                                    sphere.translateY = centerY.toDouble()
                                    sphere.translateZ = centerZ.toDouble()
                                    transformSpheres.children.add(sphere)
                                }
                            }
                        }
                    }
//                    SubScene3D.contentGroupProperty.get().children.add(transformSpheres)
                }

//                if (!timeLineBox.children.contains(transformsList))
//                    timeLineBox.children.add(transformsList)
//
//                transformsList.selectionModel.clearSelection()
//                transformsList.cellFactory = Callback {
//                    createTransformationCell()
//                }
//                transformsList.items.setAll(frameList.items[this].transformationList)
            }
//            else timeLineBox.children.remove(transformsList)
        }

//        animationPlayer.frameIndexProperty.addListener { _, _, newValue ->
//            val content = SubScene3D.contentGroupProperty.get()
//            if (content.children.contains(transformSpheres))
//                content.children.remove(transformSpheres)
//            if (newValue.toInt() >= 0 && newValue != frameList.selectionModel.selectedIndex){
//                frameList.selectionModel.select(newValue.toInt())
//            }
//        }
        animationPlayer.transformerProperty.addListener { _, _, animation ->

            frameList.items.clear()
            frameList.selectionModel.clearSelection()

            if (animation == null) {
                return@addListener
            }

            if (timeLineBox.children.contains(transformsList))
                timeLineBox.children.remove(transformsList)

            val animationFrames = animation.getFrameList()

            var totalDuration = 0

            for (animationFrame in animationFrames) {
                totalDuration += FrameTimeUtil.toFrameAsInt(animationFrame.getDuration())
            }

            val maxWidthProperty = SimpleDoubleProperty(0.0)
            maxWidthProperty.bind(Qodat.mainController.bottomBox.widthProperty().subtract(1))

            frameList.cellFactory = Callback {
                createFrameCell(totalDuration, maxWidthProperty)
            }
            frameList.items.setAll(animationFrames)
        }
    }

    private fun createFrameCell(
            totalDuration: Int,
            maxWidthProperty: SimpleDoubleProperty): ListCell<AnimationFrame> {
        val cell = ListCell<AnimationFrame>()
        cell.alignment = Pos.CENTER

        cell.itemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                val frame = cell.item
                val frameDuration = FrameTimeUtil.toFrameAsInt(frame.getDuration())
                val cellIndex = cell.index
                val first = cellIndex == 0
                cell.style =
                                "    -fx-background-insets: 0 1 0 0;\n" +
//                                "    -fx-background-radius: 0;\n" +
                                "    -fx-text-fill: #828282;\n" +
                                "    -fx-font-size: 12px;\n" +
                                "    -fx-font-weight: bold;\n" +
                                "    -fx-border-color: rgb(174, 138, 190);\n" +
                                "    -fx-border-width: 0 1 0 0"
                val transformationTypeDetails = frame.transformationList
                    .groupBy { it.getType() }
                    .mapValues { it.value.size }
                    .entries
                    .map { "${it.value}x ${it.key} transforms" }
                    .joinToString("\n")
                cell.textFill = Color.CYAN
                cell.text = cellIndex.toString()
                cell.tooltip = Tooltip(transformationTypeDetails)
                val percentage = totalDuration / frameDuration.toDouble()
                cell.prefWidthProperty().bind(maxWidthProperty
//                    .subtract(transformsList.widthProperty())
                    .divide(percentage))
            }
        }
        return cell
    }

    private fun createTransformationCell(): ListCell<Transformation> {
        val cell = ListCell<Transformation>()
        cell.alignment = Pos.CENTER

        cell.itemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                val transformation = cell.item
                val dx = transformation.getDeltaX()
                val dy = transformation.getDeltaY()
                val dz = transformation.getDeltaZ()
                val type = transformation.getType()
                val vertexGroupIndices = transformation.groupIndices.toArray(null)

                cell.textFill = Color.web("#AE8ABE")
                cell.text = "${type.name.toLowerCase().replace("_", " ")}\t($dx, $dy, $dz)"
                cell.tooltip = Tooltip(vertexGroupIndices.joinToString { "," })
            }
        }
        return cell
    }
}