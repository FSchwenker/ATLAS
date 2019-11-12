package de.atlas.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import de.atlas.data.ClassifikationResult;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class Classifier {
	
	private svm_parameter param;
	private svm_problem problem;
	private svm_model model;
	
	public Classifier(){
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		problem = new svm_problem();
	}
	public void resetParams(){
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];		
	}
	public void setSVMParams(String params){
		// parse options
		StringTokenizer st = new StringTokenizer(params);
		String[] argv = new String[st.countTokens()];
		for(int i=0;i<argv.length;i++)
			argv[i] = st.nextToken();

		for(int i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			if(++i>=argv.length)
			{
				System.err.print("unknown SVM option\n");
				break;
			}
			switch(argv[i-1].charAt(1))
			{
				case 's':
					param.svm_type = Integer.parseInt(argv[i]);
					break;
				case 't':
					param.kernel_type = Integer.parseInt(argv[i]);
					break;
				case 'd':
					param.degree = Integer.parseInt(argv[i]);
					break;
				case 'g':
					param.gamma = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'r':
					param.coef0 = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'n':
					param.nu = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'm':
					param.cache_size = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'c':
					param.C = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'e':
					param.eps = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'p':
					param.p = Double.valueOf(argv[i]).doubleValue();
					break;
				case 'h':
					param.shrinking = Integer.parseInt(argv[i]);
					break;
				case 'b':
					param.probability = Integer.parseInt(argv[i]);
					break;
				case 'w':
					++param.nr_weight;
					{
						int[] old = param.weight_label;
						param.weight_label = new int[param.nr_weight];
						System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
					}

					{
						double[] old = param.weight;
						param.weight = new double[param.nr_weight];
						System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
					}

					param.weight_label[param.nr_weight-1] = Integer.parseInt(argv[i-1].substring(2));
					param.weight[param.nr_weight-1] = Double.valueOf(argv[i]).doubleValue();
					break;
				default:
					System.err.print("unknown option\n");
			}
		}
	}
	public void trainSVM(double[][] data, double[] classes) {
		problem.l = classes.length;
		problem.y = new double[problem.l];
		problem.x = new svm_node [problem.l][data[0].length];

		for(int i=0;i<problem.l;i++)
		{
			for(int d = 0;d<data[0].length;d++){
				problem.x[i][d] = new svm_node();
				problem.x[i][d].index = d+1;
				problem.x[i][d].value = data[i][d];
			}
			problem.y[i] = classes[i];
		}

		// build model
		model = svm.svm_train(problem, param);
		
	}
	public void writeSVMProblem(double[][] data, double[] classes, File file){
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( file ) ) );
			for(int i=0;i<classes.length;i++)
			{
				out.write((int)classes[i]+ " ");
				for(int d = 0;d<data[0].length;d++){
					out.write(" "+(int)(d+1)+":"+data[i][d]);
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void saveSVM(File file) {
		try {
			svm.svm_save_model(file.getPath(), model);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void loadSVM(File file) {
		try {
			model = svm.svm_load_model(file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public ClassifikationResult classify(double[] data) {
		if(model==null)return null;
		svm_node[] x = new svm_node[data.length];
		for(int d=0;d<data.length;d++){
			x[d] = new svm_node();
			x[d].index = d+1;
			x[d].value = data[d];
		}
		//double prediction = svm.svm_predict(model, x);
		double[] prob_estimates = new double[model.nr_class];//data.length];
		double prediction = svm.svm_predict_probability(model, x, prob_estimates);
		return new ClassifikationResult((int)prediction,prob_estimates);
	}
	public int getClassCount(){
		if(model!=null)return model.nr_class;
		return 0;
	}
	public int[] getLabels(){
		if(model!=null)return model.label;
		return null;
	}

}
