package frc.robot.subsystems;

/**
 * This is custom PID Controller by Koba
 * 
 * I made this PID controller specially for autonomuse robots
 * Main reason for that, is standart PID controller would not work correctly on wheel based robots
 * 
 * Because of that, i made my own PID Controller with 4 parameters intead of 3(basic)
 * They are
 * @param p Proportional gain. Large if large difference between setpoint and target. 
 * @param i Integral gain.  Becomes large if setpoint cannot reach target quickly. 
 * @param d Derivative gain. Responds quickly to large changes in error. Small values prevents P and I terms from causing overshoot.
 * @param f Feed-forward gain. Open loop "best guess" for the output should be. Only useful if setpoint represents a rate.
 * 
 */

public class PIDF{

	private double P=0;
	private double I=0;
	private double D=0;
	private double F=0;

	private double maxIOutput=0;
	private double maxError=0;
	private double errorSum=0;

	private double maxOutput=0; 
	private double minOutput=0;

	private double setpoint=0;

	private double lastActual=0;

	private boolean firstRun=true;

	private double outputRampRate=0;
	private double lastOutput=0;

	private double outputFilter=0;

	private double setpointRange=0;
	
	public PIDF(double p, double i, double d){
		P=p; I=i; D=d;
		
		}


	public PIDF(double p, double i, double d, double f){
		P=p; I=i; D=d; F=f;
		
		}


	public void setP(double p){
		P=p;
		
	}

	public void setI(double i){
		if(I!=0){
			errorSum=errorSum*I/i;
			}
		if(maxIOutput!=0){
			maxError=maxIOutput/i;
		}
		I=i;
		
	}

	public void setD(double d){
		D=d;
		
		}

	public void setF(double f){
		F=f;
		
	}

	public void setPID(double p, double i, double d){
		P=p;D=d;
		setI(i);
		
	}

	public void setPID(double p, double i, double d,double f){
		P=p;D=d;F=f;
		setI(i);
		
	}

	
	public void setMaxIOutput(double maximum){
		maxIOutput=maximum;
		if(I!=0){
			maxError=maxIOutput/I;
		}
	}

	public void setOutputLimits(double output){
		setOutputLimits(-output,output);
	}

	public void setOutputLimits(double minimum,double maximum){
		if(maximum<minimum)return;
		maxOutput=maximum;
		minOutput=minimum;

		if(maxIOutput==0 || maxIOutput>(maximum-minimum) ){
			setMaxIOutput(maximum-minimum);
		}
	}

	

	public void setSetpoint(double setpoint){
		this.setpoint=setpoint;
	}

	public double getOutput(double actual, double setpoint){
		double output;
		double Poutput;
		double Ioutput;
		double Doutput;
		double Foutput;

		this.setpoint=setpoint;

		if(setpointRange!=0){
			setpoint=constrain(setpoint,actual-setpointRange,actual+setpointRange);
		}

		double error=setpoint-actual;

		Foutput=F*setpoint;

		Poutput=P*error;   

		if(firstRun){
			lastActual=actual;
			lastOutput=Poutput+Foutput;
			firstRun=false;
		}

		Doutput= -D*(actual-lastActual);
		lastActual=actual;
  
		Ioutput=I*errorSum;
		if(maxIOutput!=0){
			Ioutput=constrain(Ioutput,-maxIOutput,maxIOutput); 
		}    

		output=Foutput + Poutput + Ioutput + Doutput;

		if(minOutput!=maxOutput && !bounded(output, minOutput,maxOutput) ){
			errorSum=error; 
		}
		else if(outputRampRate!=0 && !bounded(output, lastOutput-outputRampRate,lastOutput+outputRampRate) ){
			errorSum=error; 
		}
		else if(maxIOutput!=0){
			errorSum=constrain(errorSum+error,-maxError,maxError);
		}
		else{
			errorSum+=error;
		}

		if(outputRampRate!=0){
			output=constrain(output, lastOutput-outputRampRate,lastOutput+outputRampRate);
		}
		if(minOutput!=maxOutput){ 
			output=constrain(output, minOutput,maxOutput);
			}
		if(outputFilter!=0){
			output=lastOutput*outputFilter+output*(1-outputFilter);
		}

		lastOutput=output;
		return output;
	}

	public double getOutput(){
		return getOutput(lastActual,setpoint);
	}

	public double getOutput(double actual){
		return getOutput(actual,setpoint);
	}

	public void reset(){
		firstRun=true;
		errorSum=0;
	}

	public void setOutputRampRate(double rate){
		outputRampRate=rate;
	}

	public void setSetpointRange(double range){
		setpointRange=range;
	}

	public void setOutputFilter(double strength){
		if(strength==0 || bounded(strength,0,1)){
		outputFilter=strength;
		}
	}

	private double constrain(double value, double min, double max){
		if(value > max){ return max;}
		if(value < min){ return min;}
		return value;
	}  

	private boolean bounded(double value, double min, double max){
		return (min<value) && (value<max);
	}

	
}