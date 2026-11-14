@echo off
chcp 65001 >nul
cd /d "%~dp0"
set "JAVA_EXE=java"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "target\zhi-mindnotes-1.0.0.jar" (
  echo [提示] 未找到 target\zhi-mindnotes-1.0.0.jar
  echo 请先在 IDEA 中执行一次 Maven package 或 Build - Rebuild Project
  pause
  exit /b 1
)
echo 正在启动 智记 MindNotes ...
echo 启动完成后会自动打开浏览器；若未打开请访问 http://localhost:8080/index.html
"%JAVA_EXE%" -jar "target\zhi-mindnotes-1.0.0.jar"
pause
