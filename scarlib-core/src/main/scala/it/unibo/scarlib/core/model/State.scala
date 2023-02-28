package it.unibo.scarlib.core.model

trait State {
    def elements(): Int

    def toSeq(): Seq[Double]

    def isEmpty(): Boolean
}

class EmptyState extends State {
    override def elements(): Int = 0

    override def toSeq(): Seq[Double] = Seq()

    override def isEmpty(): Boolean = true
}
