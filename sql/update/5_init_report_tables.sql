SELECT idKPI INTO @clientPurchasesKPI FROM puretherapie.KPI WHERE name = 'ClientPurchasesKPI';
SELECT idKPI INTO @fillingRateKPI FROM puretherapie.KPI WHERE name = 'FillingRateKPI';
SELECT idKPI INTO @newClientAndOriginKPI FROM puretherapie.KPI WHERE name = 'NewClientAndOriginKPI';
SELECT idKPI INTO @notFinalizedAppointmentKPI FROM puretherapie.KPI WHERE name = 'NotFinalizedAppointmentKPI';
SELECT idKPI INTO @surbookingKPI FROM puretherapie.KPI WHERE name = 'SurbookingKPI';
SELECT idKPI INTO @technicianACProvisionKPI FROM puretherapie.KPI WHERE name = 'TechnicianACProvisionKPI';
SELECT idKPI INTO @turnoverKPI FROM puretherapie.KPI WHERE name = 'TurnoverKPI';

INSERT INTO puretherapie.ReportType (puretherapie.ReportType.name)
VALUES ('day'),
       ('week'),
       ('month'),
       ('trimester'),
       ('semester'),
       ('year');

SELECT idReportType INTO @dayReport FROM puretherapie.ReportType WHERE name = 'day';
SELECT idReportType INTO @weekReport FROM puretherapie.ReportType WHERE name = 'week';
SELECT idReportType INTO @monthReport FROM puretherapie.ReportType WHERE name = 'month';
SELECT idReportType INTO @trimesterReport FROM puretherapie.ReportType WHERE name = 'trimester';
SELECT idReportType INTO @semesterReport FROM puretherapie.ReportType WHERE name = 'semester';
SELECT idReportType INTO @yearReport FROM puretherapie.ReportType WHERE name = 'year';

INSERT INTO puretherapie.DefaultReportTypeConfiguration (idReportType, idKPI)
VALUES (@dayReport, @clientPurchasesKPI),
       (@dayReport, @fillingRateKPI),
       (@dayReport, @newClientAndOriginKPI),
       (@dayReport, @notFinalizedAppointmentKPI),
       (@dayReport, @surbookingKPI),
       (@dayReport, @technicianACProvisionKPI),
       (@dayReport, @turnoverKPI),

       (@weekReport, @clientPurchasesKPI),
       (@weekReport, @fillingRateKPI),
       (@weekReport, @newClientAndOriginKPI),
       (@weekReport, @notFinalizedAppointmentKPI),
       (@weekReport, @surbookingKPI),
       (@weekReport, @technicianACProvisionKPI),
       (@weekReport, @turnoverKPI),

       (@monthReport, @clientPurchasesKPI),
       (@monthReport, @fillingRateKPI),
       (@monthReport, @newClientAndOriginKPI),
       (@monthReport, @notFinalizedAppointmentKPI),
       (@monthReport, @surbookingKPI),
       (@monthReport, @technicianACProvisionKPI),
       (@monthReport, @turnoverKPI),

       (@trimesterReport, @clientPurchasesKPI),
       (@trimesterReport, @fillingRateKPI),
       (@trimesterReport, @newClientAndOriginKPI),
       (@trimesterReport, @notFinalizedAppointmentKPI),
       (@trimesterReport, @surbookingKPI),
       (@trimesterReport, @technicianACProvisionKPI),
       (@trimesterReport, @turnoverKPI),

       (@semesterReport, @clientPurchasesKPI),
       (@semesterReport, @fillingRateKPI),
       (@semesterReport, @newClientAndOriginKPI),
       (@semesterReport, @notFinalizedAppointmentKPI),
       (@semesterReport, @surbookingKPI),
       (@semesterReport, @technicianACProvisionKPI),
       (@semesterReport, @turnoverKPI),

       (@yearReport, @clientPurchasesKPI),
       (@yearReport, @fillingRateKPI),
       (@yearReport, @newClientAndOriginKPI),
       (@yearReport, @notFinalizedAppointmentKPI),
       (@yearReport, @surbookingKPI),
       (@yearReport, @technicianACProvisionKPI),
       (@yearReport, @turnoverKPI);
