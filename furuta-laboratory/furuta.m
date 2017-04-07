function [sys,x0,str,ts] = furuta(t,x,u,flag,l,M,Jp,r,J,m_w,g,x_0)

alfa=Jp+M*l^2;
beta=J+M*r^2+m_w*r^2;
gamma=M*r*l;
epsilon=l*g*(M+m_w/2);

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
    sys = mdlDerivative(x,u,alfa,beta,gamma,epsilon);
            
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
function sys = mdlDerivative(x,u,alfa,beta,gamma,epsilon)

der1 = x(2);
der2 = 1/(alfa*beta+alfa^2*(sin(x(1)))^2-gamma^2*(cos(x(1)))^2)*...
       ((alfa*beta+alfa^2*(sin(x(1)))^2)*x(4)^2*sin(x(1))*cos(x(1))-...
       gamma^2*x(2)^2*sin(x(1))*cos(x(1))+...
       2*alfa*gamma*x(2)*x(4)*sin(x(1))*(cos(x(1)))^2-...
       gamma*cos(x(1))*u+...
       (alfa*beta+alfa^2*(sin(x(1)))^2)*epsilon/alfa*sin(x(1)));
der3 = x(4);
der4 = 1/(alfa*beta+alfa^2*(sin(x(1)))^2-gamma^2*(cos(x(1)))^2)*...
       (-gamma*alfa*x(4)^2*sin(x(1))*(cos(x(2)))^2-...
       gamma*epsilon*sin(x(1))*cos(x(1))+...
       gamma*alfa*x(2)^2*sin(x(1))-...
       2*alfa^2*x(2)*x(4)*sin(x(1))*cos(x(1))+...
       alfa*u);

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





