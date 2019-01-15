import org.apache.spark.sql.SparkSession
import scala.collection.mutable.HashMap
import breeze.linalg.DenseVector
import breeze.linalg.DenseMatrix
import breeze.numerics._


object Main {
  def ocurrences(list1: Array[Int]): HashMap[Int, Float] = { // prior probabilities
    val no_of_examples = list1.length;
    val attrs = list1.groupBy(identity).mapValues(_.size);
    var prob = HashMap[Int, Float]();

    for ((k,v) <- attrs) {
      prob(k) = v/(no_of_examples).toFloat;
    }

    prob
  }


  def naiveBayes(
    training  : DenseMatrix[Int],
    outcome   : DenseVector[Int],
    new_sample: DenseVector[Int],
    rows      : Int,
    cols      : Int
  ) = {
    val outcome_array = outcome.toArray;
    val classes = outcome_array.toSet;
    // initializing the dictionary
    var likelihoods = HashMap[Int, HashMap[Int, DenseVector[Int]]]();
    var likelihoods_aux = HashMap[Int, HashMap[Int, HashMap[Int, Float]]]();
    val class_probabilities = ocurrences(outcome_array)

    for(cls <- classes) {// probabilities per feature per class
      likelihoods(cls) = HashMap();
      likelihoods_aux(cls) = HashMap();
      // taking samples of only 1 class at a time
      val indexes = outcome.findAll(_ == cls);
      val subsets = training(indexes, ::).toDenseMatrix;
      val r = subsets.rows;
      val c = subsets.cols;

      for (j: Int <- 0 to c-1){
        likelihoods(cls)(j) = subsets(::, j);
      }
    }

    for(cls <- classes) {
      for (j: Int <- 0 to cols-1) {
        likelihoods_aux(cls)(j) = ocurrences((likelihoods(cls)(j)).toArray);
      }
    }

    val results = HashMap[Int, Float]();

    for(cls <- classes){
      var class_probability = class_probabilities(cls);

      for(i <- 0 to new_sample.length-1) {
        val relative_values = likelihoods_aux(cls)(i);

        class_probability *= (
          if (relative_values.contains(new_sample(i)))
            relative_values(new_sample(i))
          else
            0
        )

        results(cls) = class_probability;
      }
    }

    println(results);
  }


  def main(args: Array[String]) = {
    val training = DenseMatrix((1,0,1,1),(1,1,0,0),(1,0,2,1),
      (0,1,1,1),(0,0,0,0),(0,1,2,1),(0,1,2,0),(1,1,1,1));
    val outcome = DenseVector(0,1,1,1,0,1,0,1);
    val new_sample = DenseVector(1,0,1,0);

    naiveBayes(training, outcome, new_sample, training.rows, training.cols);
  }
}
