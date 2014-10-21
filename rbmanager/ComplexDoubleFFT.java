package rbmanager;

public class ComplexDoubleFFT extends ComplexDoubleFFT_Mixed
{

     public double norm_factor;
     private double wavetable[];
     private int ndim;


     public ComplexDoubleFFT(int n)
     {
          ndim = n;
          norm_factor = n;
          if(wavetable == null || wavetable.length !=(4*ndim+15))
          {
              wavetable = new double[4*ndim + 15];
          }
          cffti(ndim, wavetable);
     }


     public void ft(double x[])
     {
         if(x.length != 2*ndim) 
              throw new IllegalArgumentException("The length of data can not match that of the wavetable");
         cfftf(ndim, x, wavetable); 
     }


     public void ft(Complex1D x)
     {
         if(x.x.length != ndim)
              throw new IllegalArgumentException("The length of data can not match that of the wavetable");
         double[] y = new double[2*ndim];
         for(int i=0; i<ndim; i++)
         {
              y[2*i] = x.x[i];
              y[2*i+1] = x.y[i];
         }
         cfftf(ndim, y, wavetable);
         for(int i=0; i<ndim; i++)
         {
              x.x[i]=y[2*i];
              x.y[i]=y[2*i+1];
         }
     }


     public void bt(double x[])
     {
         if(x.length != 2*ndim)
              throw new IllegalArgumentException("The length of data can not match that of the wavetable");
         cfftb(ndim, x, wavetable);
     }


     public void bt(Complex1D x)
     {
         if(x.x.length != ndim)
              throw new IllegalArgumentException("The length of data can not match that of the wavetable");
         double[] y = new double[2*ndim];
         for(int i=0; i<ndim; i++)
         {
              y[2*i] = x.x[i];
              y[2*i+1] = x.y[i];
         }
         cfftb(ndim, y, wavetable);
         for(int i=0; i<ndim; i++)
         {
              x.x[i]=y[2*i];
              x.y[i]=y[2*i+1];
         }
     }
}
