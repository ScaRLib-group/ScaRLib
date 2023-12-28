package it.unibo.scarlib.vmas

case class VmasStateDescriptor(hasPosition: Boolean = true, hasVelocity: Boolean = true, positionDimensions: Int = 2,
                               velocityDimensions: Int = 2, lidarDimension: Int = 1, lidars: Seq[Int]) {
    def getSize: Int = {
        var totalSize = 0
        if (hasPosition) totalSize += positionDimensions
        if (hasVelocity) totalSize += velocityDimensions
        if (lidars.nonEmpty) totalSize += lidars.sum * lidarDimension
        return totalSize
    }
}
