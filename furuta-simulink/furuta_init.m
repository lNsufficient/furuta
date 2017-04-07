%=======================================================================
%
% Parameters for the Furuta Pendulum Model
%
% Created 990615 by Johan Åkesson
% Revised 020206 by Johan Åkesson
%
%=======================================================================


%=====================================================================
%
% Process constants
%
%=====================================================================
h=0.01; %Sampling interval
g=9.81;
Fscomp_pos=0.01;  %0.25;
Fscomp_neg=0.01;  %0.35;

%==========================================================================
%
% Linearized Furuta Pendulum model
%
%==========================================================================
lp = 0.421;      % Pendulum length
M = 0.015;       % Pendulum weight mass
mpa = 0.02;      % Pendulum mass
r = 0.245;       % Arm length
r_cm = 0.044;    % Distance from center of rotation of arm to center of
                 % mass
ma = 0.165;      % Mass of arm  	     
wa = 1.133*2*pi; % Natural frequency of arm with resepect to center
                 % of rotation
wp = 0.836*2*pi; % Natural frequency of arm with resepect to center
                 % of rotation
Jm = 0.0000381;  % Moment of inertia of motor 
l = (mpa/2+M)/(mpa+M)*lp; % Distance from pivot point to cm of
                          % pendulum
Jp = (mpa/3+M)*lp^2;      % Moment of inertia of pendulm
Jarm = ma*g*r_cm/wa^2;    % Moment of inertia of arm assembly
Ja = Jarm + Jm + (mpa+M)*r^2; % Moment of inertia of arm and
                              % pendulum with respect to center of
                              % rotation of arm

			      
x0 = [0.1 0 0 0]';
phidot_lin = 0;


alfa = Ja;
beta=Jp;
gamma=(mpa+M)*r*l;
delta=(mpa+M)*l*g;

Afc=[0 1 0 0;
     alfa*(beta*phidot_lin+delta)/(alfa*beta-gamma^2) 0 0 0;
     0 0 0 1;
     -gamma*(beta*phidot_lin+delta)/(alfa*beta-gamma^2) 0 0 0];
Bfc=[0 -gamma/(alfa*beta-gamma^2) 0 beta/(alfa*beta-gamma^2)]';
Cfc=eye(4);
Dfc=zeros(4,1);

% Discretize the system
[Afd Bfd Cfd Dfd]=ssdata(c2d(ss(Afc,Bfc,Cfc,Dfc),h));

%=====================================================================
%
% A state feed back ontroller
%
%=====================================================================

% Design parameters for the LQ design
Q_l = diag([1/0.1^2 1/1^2 1/0.3^2 1/2^2]);
R_l = 100;


% Calculate the static gain
L_stat = dlqr(Afd,Bfd,Q_l,R_l,zeros(4,1));


