
@echo off

::set OPTION=-compilerMode viaJavaSource
::set OPTION=-compilerMode directClassFiles
set OPTION=-compilerMode simulaClassLoader

set OPTION=%OPTION% -noConsole
set OPTION=%OPTION% -runtimeUserDir C:\GitHub\SimulaCompiler2\SimulaTestBatch\src\simulaSamples
set OPTION=%OPTION% -sourceFileDir C:\GitHub\SimulaCompiler2\SimulaTestBatch/src/simulaTestBatch
::echo The Options used: %OPTION%
::pause

set FILES=SimulaTest.sim
set FILES=%FILES% simtst01.sim simtst02.sim simtst03.sim simtst04.sim simtst05.sim simtst06.sim simtst07.sim simtst08.sim simtst09.sim
set FILES=%FILES% simtst10.sim simtst11.sim simtst12.sim simtst13.sim simtst14.sim simtst15.sim simtst16.sim simtst17.sim simtst18.sim simtst19.sim
set FILES=%FILES% simtst20.sim simtst21.sim simtst22.sim simtst23.sim simtst24.sim simtst25.sim simtst26.sim simtst27.sim simtst28.sim simtst29.sim
set FILES=%FILES% simtst30a.sim simtst30.sim simtst31.sim simtst32.sim simtst33.sim simtst34.sim simtst35.sim simtst36.sim simtst37.sim simtst38.sim simtst39.sim
set FILES=%FILES% p40b.sim p40a.sim p40c.sim simtst40.sim p41.sim simtst41.sim
set FILES=%FILES% simtst42.sim simtst43.sim simtst44.sim simtst45.sim simtst46.sim simtst47.sim simtst48.sim simtst49.sim simtst50.sim
set FILES=%FILES% simtst51.sim simtst52.sim simtst53.sim simtst54.sim simtst55.sim simtst56.sim simtst57.sim simtst58.sim simtst59.sim
set FILES=%FILES% simtst60.sim simtst61.sim simtst62.sim simtst63.sim simtst64.sim simtst65.sim simtst66.sim simtst67.sim simtst68.sim simtst69.sim
set FILES=%FILES% simtst70.sim simtst71.sim simtst72.sim simtst73.sim simtst74.sim simtst75.sim simtst76.sim simtst77.sim simtst78.sim simtst79.sim
set FILES=%FILES% simtst80.sim simtst81.sim simtst82.sim simtst83.sim simtst84.sim simtst85.sim Separat.sim simtst86.sim simtst87.sim simtst88.sim simtst89.sim
set FILES=%FILES% simtst90.sim simtst91.sim simtst92.sim simtst93.sim simtst94.sim simtst95.sim simtst96.sim simtst97.sim simtst98.sim simtst99.sim
set FILES=%FILES% simtst100.sim simtst101.sim simtst102.sim simtst103.sim simtst104.sim simtst105.sim simtst107.sim simtst108.sim simtst109.sim
set FILES=%FILES% simtst110.sim simtst111.sim simtst112.sim simtst113.sim simtst114.sim simtst115.sim simtst116.sim simtst117.sim simtst118.sim
set FILES=%FILES% ExternalClass1.sim ExternalClass2.sim simtst119.sim simtst120.sim
set FILES=%FILES% simtst121.sim simtst122.sim simtst123.sim simtst124.sim simtst125.sim simtst126.sim simtst127.sim simtst128.sim Precompiled129.sim simtst129.sim
set FILES=%FILES% simtst130.sim simtst131.sim simtst132.sim simtst133.sim simtst134.sim simtst135.sim simtst136.sim simtst137.sim simtst138.sim simtst139.sim
set FILES=%FILES% simtst140.sim simtst141.sim simtst142.sim simtst143.sim Precompiled144.sim simtst144.sim simtst145.sim simtst146.sim simtst147.sim simtst148.sim simtst149.sim
set FILES=%FILES% simtst150.sim simtst151.sim simtst152.sim simtst153.sim simtst154.sim Pre155.sim simtst155.sim simtst156.sim simtst157.sim simtst158.sim simtst159.sim
set FILES=%FILES% simtst160.sim simtst161.sim simtst162.sim simtst163.sim

set FILES=%FILES% RT_ErrorTest.sim simerr01.sim simerr02.sim PrecompiledClass.sim simerr03.sim
set FILES=%FILES% PrecompiledProcedure.sim simerr04.sim simerr05.sim simerr06.sim simerr07.sim simerr08.sim simerr09.sim simerr10.sim

cd C:\Users\omyhr\Simula\Simula-2.0

::echo The FILES used: %FILES%
::pause

java -jar simula.jar %OPTION% %FILES%
::pause

echo The Options used: %OPTION%

pause