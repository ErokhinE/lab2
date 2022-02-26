section .text

%define sys_exit 60
%define read 0
%define write 1
%define stdin 0
%define stdout 1
%define new_line 0xA

; Принимает код возврата и завершает текущий процесс
exit: 
    mov rax, sys_exit
    syscall

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    xor rax, rax
.lp:
    cmp byte[rdi+rax], 0
    je .end
    inc rax
    jmp .lp
.end:
    ret


; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    mov rsi, rdi
    push rsi
    call string_length
    pop rsi
    mov rdx, rax
    mov rax, write
    mov rdi, stdout
    syscall
    ret


; Переводит строку (выводит символ с кодом 0xA)
print_newline:
  mov rdi, new_line

; Принимает код символа и выводит его в stdout
print_char:
    mov rax, write
    push rdi
    mov rsi, rsp
    mov rdi, stdout
    mov rdx, 1; length
    syscall
    pop rdi
    ret

; Выводит знаковое 8-байтовое число в десятичном формате
print_int:
    mov rax, rdi
    test rax, rax
    jns .add
    mov rdi, '-'
    push rax
    call print_char
    pop rax
    neg rax
    mov rdi, rax
    .add:

; Выводит беззнаковое 8-байтовое число в десятичном формате 
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    mov r8, rsp
    mov r9, 10
    mov rax, rdi
    dec rsp
    mov byte[rsp], 0

    .lp:
        dec rsp
        xor rdx, rdx
        div r9
        or rdx, 0x30
        mov byte[rsp], dl
        test rax, rax
        jnz .lp
    .print:
        mov rdi, rsp
        call print_string
        mov rsp, r8
        ret



; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    xor rcx, rcx
    .compare:
        mov r8b, byte[rdi+rcx]
        mov r9b, byte[rsi+rcx]
        cmp r8b, r9b
        jne .uncorrect
        test r8b, r8b
        jz .success
        inc rcx
        jmp .compare
    .success:
        mov rax, 1
        ret
    .uncorrect:
        xor rax, rax
        ret


; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    dec rsp
    mov rax, read
    mov rdi, stdin
    mov rdx, 1
    mov rsi, rsp
    syscall
    test rax, rax
    je .rtn
    mov rax, [rsp]

    .rtn:
    inc rsp
    ret 


; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале, .
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax
; Эта функция должна дописывать к слову нуль-терминатор

read_word:
    xor rcx, rcx
    .lp:
        cmp rcx, rsi
        jae .not_fit
        push rdi
        push rcx
        call read_char
        pop rcx
        pop rdi
        cmp al, 0xA
        je .hsymbol
        cmp al, 0x9
        je .hsymbol
        cmp al, 0x20
        je .hsymbol
        cmp al, byte 0
        je .end
        mov [rdi + rcx], al
        inc rcx
        jmp .lp
    .not_fit:
        xor rax, rax
        ret
    .hsymbol:
        cmp rcx, 0
        je .lp
    .end:
        mov [rdi + rcx], byte 0
        mov rax, rdi
        mov rdx, rcx
        ret


; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    xor rax, rax
    xor rsi, rsi
    mov r8, 10
    xor rcx, rcx

 .read:
    mov sil, [rdi+rcx]
    cmp sil, '0'
    jb .exit
    cmp sil, '9'
    ja .exit
    inc rcx
    sub sil, '0'
    mul r8
    add rax, rsi
    jmp .read

 .exit:
    mov rdx, rcx
    ret






; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был) 
; rdx = 0 если число прочитать не удалось
parse_int:
    xor rax, rax
    cmp byte[rdi], '-'
    je .sign
    jmp parse_uint

    .sign:
        inc rdi ; считаем знак
        call parse_uint  
        cmp rdx, 0
        je .exit
        neg rax
        inc rdx ; за знак

    .exit:
    ret



; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    push rdi
    push rsi
    push rdx
    call string_length
    pop rdx
    pop rsi
    pop rdi

    cmp rax, rdx
    je .exit
    xor rax, rax
.lp:
    cmp rax, rdx
    je .less

    mov bl, byte [rdi+rax]
    mov byte [rsi + rax], bl
    cmp byte [rsi + rax], 0
    je .exit

    inc rax
    jmp .lp
.less:
    xor rax, rax
.exit:
    ret
