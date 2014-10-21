package rbmanager;

public class RealDoubleFFT_Odd_Odd extends RealDoubleFFT_Even_Odd
{

     public RealDoubleFFT_Odd_Odd(int n)
     {
          super(n);
     }


     public void ft(double x[])
     {
         sinqf(ndim, x, wavetable);
     }


     public void bt(double x[])
     {
         sinqb(ndim, x, wavetable);
     }


     void sinqf(int n, double x[], double wtable[])
     {
         int     k;
         double  xhold;
         int     kc, ns2;

         if(n==1) return;
         ns2=n / 2;
         for(k=0; k<ns2; k++)
         {
	      kc=n-k-1;
	      xhold=x[k];
	      x[k]=x[kc];
	      x[kc]=xhold;
         }
         cosqf(n, x, wtable);
         for(k=1; k<n; k+=2) x[k]=-x[k];
     } 


     void sinqb(int n, double x[], double wtable[])
     {
          int     k;
          double  xhold;
          int     kc, ns2;

          if(n<=1)
          {
	      x[0]*=4;
	      return;
          }
          ns2=n / 2;
          for(k=1; k<n; k+=2) x[k]=-x[k];
          cosqb(n, x, wtable);
          for(k=0; k<ns2; k++)
          {
	      kc=n-k-1;
	      xhold=x[k];
	      x[k]=x[kc];
	      x[kc]=xhold;
          }
     } 

/*
     void sinqi(int n, double wtable[])
     {
          cosqi(n, wtable);
     }
*/
}
