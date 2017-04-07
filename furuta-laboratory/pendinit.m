%=======================================================================
%
% Initialization of Simulink environment
%
% Created 990615 by Johan Akesson
% Modified 2001 by Bo Lincoln
% Modified Feb 2002 Johan Åkesson
%
%=======================================================================

%=======================================================================
%
% Conversion constants
%
%======================================================================

% Theta top
k1=0.058;  %0.0928;     %0.058;
%offs1=0.6189; %0.512
offs1=0.7792; %0.512

% Theta velocity top
k2=0.68; %1.088     %0.68;
offs2=0; % 0;

k4m=0.603;
offs4m=0;
k5m=0.48;
offs5m=0.06;

% Phi
k4r=2.56;
offs4r=0;

% Phi velocity
k5r=2.0;
offs5r=0.0708;

ku=1.40;

offsJoyX=-3.8364; 
kJoyX=6;
offsJoyY=0;
kJoyY=1;

k360=0.3091;
offs360=5.1763; % 4.984;
kdot360=3.76;
offsdot360=-0.022;

% Tracking constants
pos_track = 1.0434;
neg_track = 1.0668;

%=====================================================================
%
% Process constants
%
%=====================================================================

h=0.01; %Sampling interval
w0=6.7;
g=9.81;
ktheta=0.05;  % Damp constant
kx=0.1;       % Damp constant
bias=0.0;
Fscomp_pos=0.16;  %0.25;
Fscomp_neg=0.16;  %0.35;
Fsobs=0.12;
r=0.235;
l=0.413;
m=0.02;

% Initial conditions
theta0=0;%-0.05;
thetadot0=0;%-0.03;


%==========================================================================
%
% Furuta pendulum model
%
%==========================================================================
l=0.413;
M=0.01;
Jp=0.0009;
r=0.235;
J=0.05;
m=0.02;
g=9.81;
x0=[0.1 0 0 0]';
phidot_lin=0;


alfa=Jp+M*l^2;
beta=J+M*r^2+m*r^2;
gamma=M*r*l;
epsilon=l*g*(M+m/2);

Afc=[0 1 0 0;
     (beta*epsilon+alfa*beta*phidot_lin^2)/(alfa*beta-gamma^2) 0 0 0;
     0 0 0 1;
     -(gamma*epsilon+alfa*gamma*phidot_lin^2)/(alfa*beta-gamma^2) 0 0 0];
Bfc=[0 -gamma/(alfa*beta-gamma^2)*g 0 alfa/(alfa*beta-gamma^2)*g]';
Cfc=eye(4);
Dfc=zeros(4,1);
[Afd Bfd Cfd Dfd]=ssdata(c2d(ss(Afc,Bfc,Cfc,Dfc),h));

fur_tf_c = tf(ss(Afc,Bfc,Cfc,Dfc));

Afc_r=[Afc(1:2,1:2) Afc(1:2,4); Afc(4,1:2) Afc(4,4)];
Bfc_r=[Bfc(1:2); Bfc(4)];
Cfc_r=eye(3);
Dfc_r=zeros(3,1);
[Afd_r Bfd_r Cfd_r Dfd_r]=ssdata(c2d(ss(Afc_r,Bfc_r,Cfc_r,Dfc_r),h));

Q = diag([100 1 10e-10 10]);
R = 100; 
Lnopos = dlqr(Afd,Bfd,Q,R);
Q = diag([100 1 10 10]);
Lpos = dlqr(Afd,Bfd,Q,R);



clear i








