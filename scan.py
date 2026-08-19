import re
files = [
 "app/src/main/java/com/salat/gbinder/ui/KeyBindingDialog.kt",
 "app/src/main/java/com/salat/gbinder/ui/FuncCustomDialog.kt",
 "app/src/main/java/com/salat/gbinder/features/configurator/RenderConfigurator.kt",
 "app/src/main/java/com/salat/gbinder/MainActivity.kt",
 "app/src/main/java/com/salat/gbinder/gmp/MediaSessionPipeline.kt",
 "app/src/main/java/com/salat/gbinder/util/DriveModeUtl.kt",
 "app/src/main/java/com/salat/gbinder/entity/DisplayDriveMode.kt",
 "app/src/main/java/com/salat/gbinder/util/DriveModeNotifStore.kt",
 "app/src/main/java/com/salat/gbinder/mappers/KeyCodeMap.kt",
]
skip_re = re.compile(r'^\s*(//|/\*|\*|import|package)')
for f in files:
    print("=== "+f+" ===")
    for i,line in enumerate(open(f),1):
        if skip_re.match(line): continue
        if 'R.string' in line or 'android.R' in line or 'stringResource' in line: continue
        if 'Log.' in line or 'Timber' in line or 'AppLogger' in line or 'debugLog' in line or '.log(' in line: continue
        for m in re.finditer(r'"([^"]*[A-Za-z][^"]*)"', line):
            s=m.group(1)
            if re.match(r'^(com\.|android\.|http|https|file|content|intent|action|package|permission)', s): continue
            if s.startswith('%') or s.startswith('$') or s.startswith('{') or s.startswith('['): continue
            if s in ('id','value','area','result','null','file','int','float','string','boolean','long','double','byte','short','char','true','false','+','->','Zone'): continue
            print(f"  {i}: {line.strip()[:110]}")
