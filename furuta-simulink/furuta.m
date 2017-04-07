function [sys,x0,str,ts] = furuta(t,x,u,flag,lp,M,mpa,r,ma,Jarm,Jm,g,x_0)

l = (mpa/2+M)/(mpa+M)*lp; % Distance from pivot point to cm of
                          % pendulum
Jp = (mpa/3+M)*lp^2;      % Moment of inertia of pendulm

Ja = Jarm + Jm + (mpa+M)*r^2; % Moment of inertia of arm and
                              % pendulum with respect to center of
                              % rotation of arm
			      
alfa = Ja;
beta=Jp;
gamma=(mpa+M)*r*l;
delta=(mpa+M)*l*g;


alfao = 0.00260569;
betao = 0.05165675;
gammao = 9.7055e-04;
epsilon = 0.0810306;

switch flag,

  %%%%%%%%%%%%%%%%%%
  % Initialization %
  %%%%%%%%%%%%%%%%%%
  case 0,
     [sys,x0,str,ts] = mdlInitializeSizes(x_0);
     
  %%%%%%%%%%%%%
  % Derivativ %
  %%%%%%%%%%%%%
  case 1,
    sys = mdlDerivative(x,u,alfa,beta,gamma,delta,alfao,betao,gammao,epsilon);
            
  %%%%%%%%%%
  % Output %
  %%%%%%%%%%
  case 3,                                                
    sys = mdlOutputs(x);

  %%%%%%%%%%%%%
  % Terminate %
  %%%%%%%%%%%%%
  case {2,4,9},                                                
     sys = []; % do nothing
     
  %%%%%%%%%%%%%%%%%%%%
  % Unexpected flags %
  %%%%%%%%%%%%%%%%%%%%
  otherwise
    error(['unhandled flag = ',num2str(flag)]);
end

%end dsfunc

%
%=======================================================================
% mdlInitializeSizes
% Return the sizes, initial conditions, and sample times for the S-function.
%=======================================================================
%
function [sys,x0,str,ts] = mdlInitializeSizes(x_0)

sizes = simsizes;
sizes.NumContStates  = 4;
sizes.NumDiscStates  = 0;
sizes.NumOutputs     = 4;
sizes.NumInputs      = 1;
sizes.DirFeedthrough = 0;
sizes.NumSampleTimes = 1;

sys = simsizes(sizes);

x0  = x_0;
str = [];
ts  = [0 0]; 

% end mdlInitializeSizes
%
%=======================================================================
% mdlDerivative
% 
%=======================================================================
%
function sys = mdlDerivative(x,u,alfa,beta,gamma,delta,alfao,betao, ...
			     gammao,epsilon) 


der1 = x(2);

der2 = 1/(alfa*beta-gamma^2+(beta^2+gamma^2)*(sin(x(1)))^2)*...
(2*beta*gamma*sin(x(1))*(cos(x(1)))^2*x(2)*x(4)-...
gamma^2*sin(x(1))*cos(x(1))*x(2)^2+...
beta*(alfa+beta*(sin(x(1)))^2)*sin(x(1))*cos(x(1))*x(4)^2+...
delta*(alfa+beta*(sin(x(1)))^2)*sin(x(1))-...
gamma*cos(x(1))*u);

der3 = x(4);

der4 = 1/(alfa*beta-gamma^2+(beta^2+gamma^2)*(sin(x(1)))^2)*...
(-2*beta^2*sin(x(1))*cos(x(1))*x(2)*x(4)+...
beta*gamma*sin(x(1))*x(2)^2-...
beta*gamma*sin(x(1))*(cos(x(1)))^2*x(4)^2-...
delta*gamma*sin(x(1))*cos(x(1))+beta*u);


%der1 = x(2);
%der2 = 1/(alfao*betao+alfao^2*(sin(x(1)))^2-gammao^2*(cos(x(1)))^2)*...
%       ((alfao*betao+alfao^2*(sin(x(1)))^2)*x(4)^2*sin(x(1))*cos(x(1))-...
%       gammao^2*x(2)^2*sin(x(1))*cos(x(1))+...
%       2*alfao*gammao*x(2)*x(4)*sin(x(1))*(cos(x(1)))^2-...
%       gammao*cos(x(1))*u+...
%       (alfao*betao+alfao^2*(sin(x(1)))^2)*epsilon/alfao*sin(x(1)));
%der3 = x(4);
%der4 = 1/(alfao*betao+alfao^2*(sin(x(1)))^2-gammao^2*(cos(x(1)))^2)*...
%       (-gammao*alfao*x(4)^2*sin(x(1))*(cos(x(1)))^2-...
%       gammao*epsilon*sin(x(1))*cos(x(1))+...
%       gammao*alfao*x(2)^2*sin(x(1))-...
%       2*alfao^2*x(2)*x(4)*sin(x(1))*cos(x(1))+...
%       alfao*u);




sys = [der1 der2 der3 der4]';

%end mdlDerivative
%
%=======================================================================
% mdlOutputs
% Return Return the output vector for the S-function
%=======================================================================
%
function sys = mdlOutputs(x)

sys = [x(1) x(2) x(3) x(4)]';   %Utsignal

%end mdlOutputs





