#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <stdlib.h>
#include <jni.h>
#include "android/log.h"

static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

int set_opt(int fd,int nSpeed, int nBits, char nEvent, int nStop);
int open_port(int fd,int comport);
unsigned char uart_cmd(unsigned char *buffer,int tx, int rx);

int set_opt(int fd,int nSpeed, int nBits, char nEvent, int nStop)
{
	/* 五个参量 fd打开文件 speed设置波特率 bit数据位设置   neent奇偶校验位 stop停止位 */
	struct termios newtio,oldtio;
	if ( tcgetattr( fd,&oldtio) != 0) {
		perror("SetupSerial 1");
		return -1;
	}
	bzero( &newtio, sizeof( newtio ) );
	newtio.c_cflag |= CLOCAL | CREAD;
	newtio.c_cflag &= ~CSIZE;
	newtio.c_oflag &= ~(ONLCR | OCRNL); //
	newtio.c_iflag &= ~(IXON | IXOFF | IXANY);    //
	switch( nBits )
	{
		case 7:
			newtio.c_cflag |= CS7;
			break;
		case 8:
			newtio.c_cflag |= CS8;
			break;
	}
	switch( nEvent )
	{
		case 'O':
			newtio.c_cflag |= PARENB;
			newtio.c_cflag |= PARODD;
			newtio.c_iflag |= (INPCK | ISTRIP);
			break;
		case 'E':
			newtio.c_iflag |= (INPCK | ISTRIP);
			newtio.c_cflag |= PARENB;
			newtio.c_cflag &= ~PARODD;
			break;
		case 'N':
			newtio.c_cflag &= ~PARENB;
			break;
	}
	switch( nSpeed )
	{
		case 2400:
			cfsetispeed(&newtio, B2400);
			cfsetospeed(&newtio, B2400);
			break;
		case 4800:
			cfsetispeed(&newtio, B4800);
			cfsetospeed(&newtio, B4800);
			break;
		case 9600:
			cfsetispeed(&newtio, B9600);
			cfsetospeed(&newtio, B9600);
			break;
		case 19200:
        	cfsetispeed(&newtio, B19200);
        	cfsetospeed(&newtio, B19200);
        	break;
		case 115200:
			cfsetispeed(&newtio, B115200);
			cfsetospeed(&newtio, B115200);
			break;
		default:
			cfsetispeed(&newtio, B9600);
			cfsetospeed(&newtio, B9600);
			break;
	}
	if( nStop == 1 )
		newtio.c_cflag &= ~CSTOPB;
	else if ( nStop == 2 )
		newtio.c_cflag |= CSTOPB;
	newtio.c_cc[VTIME] =5; //测试时该大一点
	newtio.c_cc[VMIN] = 0;//set min read byte!
	tcflush(fd,TCIFLUSH);
	if((tcsetattr(fd,TCSANOW,&newtio))!=0)
	{
		perror("com set error");
		return -1;
	}
	printf("set done!\n");
	return 0;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_viroyal_com_dev_nfcserial_SerialPort_open(JNIEnv *env, jclass thiz, jstring path,
                                                    jint baudrate, jint flags) {
    int fd;
    jobject mFileDescriptor;

    if((fd = open_port(fd,flags)) < 0){
        LOGD("open fd error %d", fd);
        return NULL;
    }

    if(set_opt(fd,baudrate,8,'N',1) < 0)
    {
        LOGD("set_opt error", fd);
    	perror("set_opt error");
    	return NULL;
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(mFileDescriptor, descriptorID, (jint)fd);
    }

    return mFileDescriptor;
}

extern "C"
JNIEXPORT void JNICALL
Java_viroyal_com_dev_nfcserial_SerialPort_close(JNIEnv *env, jobject thiz) {

    jclass SerialPortClass = env->GetObjectClass(thiz);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(thiz, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);

    close(descriptor);

}

int open_port(int fd,int comport)
{

    if(comport == 0){
        fd = open("/dev/ttyS4", O_RDWR|O_NOCTTY|O_NDELAY);
        if(fd == -1){
            perror("Can not start port");
        }
    } else if(comport==1){
        fd = open( "/dev/ttyS3", O_RDWR|O_NOCTTY|O_NDELAY);

    	if (-1 == fd){
    		perror("Can't Open Serial Port");
    			return(-1);
    	}else
    		printf("open ttyS3 .....\n");
    }else if (comport==2){
    	fd = open( "/dev/ttyUSB0", O_RDWR|O_NOCTTY|O_NDELAY);
    	if (-1 == fd){
    		perror("Can't Open Serial Port");
    		return(-1);
    	}
    	else
    		printf("open ttyUSB0 .....\n");
    }
    if(fcntl(fd, F_SETFL, 0) < 0)
    	printf("fcntl failed!\n");
    else
    	printf("fcntl=%d\n",fcntl(fd, F_SETFL,0));
    if(isatty(STDIN_FILENO)==0)
    	printf("standard input is not a terminal device\n");
    else
    	printf("isatty success!\n");
    printf("fd-open=%d\n",fd);
    return fd;
}