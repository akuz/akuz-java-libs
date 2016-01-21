package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.learning.FiniteColors;
import me.akuz.mnist.digits.visor.transform.SplitLastDim;
import me.akuz.mnist.digits.visor.transform.YpCbCr;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final Tensor image,
			final int colorCountY,
			final int colorCountC,
			final int iterCount) throws IOException {
		
		// transform colors
		final YpCbCr layer0 = new YpCbCr(YpCbCr.Mode.NORMALIZE, image.shape);
		layer0.setInput(image);
		layer0.infer(false);
		
		// split channels
		final SplitLastDim layer1 = new SplitLastDim(layer0.output.shape, 1);
		layer1.setInput(layer0.output);
		layer1.infer(false);

		// finite colors (Y)
		final FiniteColors layer2Y = new FiniteColors(layer1.output1.shape, colorCountY);
		layer2Y.setInput(layer1.output1);

		// finite colors (C)
		final FiniteColors layer2C = new FiniteColors(layer1.output2.shape, colorCountC);
		layer2C.setInput(layer1.output2);
		
		// perform learning
		for (int i=0; i<iterCount; i++) {
			System.out.println(i);
			layer2Y.infer(false);
			layer2Y.learn();
			layer2C.infer(false);
			layer2C.learn();
		}
		
		System.out.println("dream");
		final Tensor dream = new Tensor(image.shape);
		layer0.setInput(dream);
		layer2Y.dream();
		layer2C.dream();
		layer1.dream();
		layer0.dream();
		
		TensorFiles.saveImage_sRGB(dream, PREFIX + 
				"_" + colorCountY + 
				"_" + colorCountC + 
				".png");
		
		System.out.println();
		System.out.println("DONE " + colorCountY + " x " + colorCountC + " colors.");
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		
//		final Tensor image = TensorGen.colourSineImage(150, 200);
//		final DenseTensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/baz.jpg");
		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/mount.png");
//		final Tensor image = TensorFiles.loadImage("/Users/andrey/Desktop/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=64; colorCount*=2) {
			approximate(
					image, 
					colorCount, 
					Math.max(2, colorCount / 4),
					10);
		}
	}

}

